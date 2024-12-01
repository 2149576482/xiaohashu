package com.arnanzz.xiaohashu.user.relation.biz.consumer;

import com.arnanzz.framework.common.util.DateUtils;
import com.arnanzz.framework.common.util.JsonUtils;
import com.arnanzz.xiaohashu.user.relation.biz.constant.MQConstants;
import com.arnanzz.xiaohashu.user.relation.biz.constant.RedisKeyConstants;
import com.arnanzz.xiaohashu.user.relation.biz.domain.entity.FansDO;
import com.arnanzz.xiaohashu.user.relation.biz.domain.entity.FollowingDO;
import com.arnanzz.xiaohashu.user.relation.biz.domain.mapper.FansDOMapper;
import com.arnanzz.xiaohashu.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.arnanzz.xiaohashu.user.relation.biz.model.dto.FollowUserMqDTO;
import com.arnanzz.xiaohashu.user.relation.biz.model.dto.UnfollowUserMqDTO;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 关注、取关 MQ 消费者
 **/
@Component
@RocketMQMessageListener(consumerGroup = "xiaohashu_group", //Group组
        topic = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW, // 主题
        consumeMode = ConsumeMode.ORDERLY // 设置为顺序消费
)
@Slf4j
public class FollowUnfollowConsumer implements RocketMQListener<Message> {

    @Resource
    private FollowingDOMapper followingDOMapper;

    @Resource
    private FansDOMapper fansDOMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // guava令牌桶
    @Resource
    private RateLimiter rateLimiter;
    @Override
    public void onMessage(Message message) {

        // 流量消峰，通过获取令牌，如果没有令牌可用，将阻塞，直到获得令牌
        rateLimiter.acquire();

        // 消息体
        String bodyJsonStr = new String(message.getBody());
        // 标签
        String tags = message.getTags();
        log.info("==> FollowUnfollowConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);

        // 根据 MQ 标签 判断操作类型
        if (Objects.equals(tags, MQConstants.TAG_FOLLOW)) {
            handleFollowTagMessage(bodyJsonStr);
        } else {
            handleUnfollowTagMessage(bodyJsonStr);
        }
    }

    /**
     * 取关
     */
    private void handleUnfollowTagMessage(String bodyJsonStr) {
        // 将消息体 Json 字符串转为 DTO 对象
        UnfollowUserMqDTO unfollowUserMqDTO = JsonUtils.parseObject(bodyJsonStr, UnfollowUserMqDTO.class);
        // 判空
        if (Objects.isNull(unfollowUserMqDTO)) return;
        Long userId = unfollowUserMqDTO.getUserId();
        Long unfollowUserId = unfollowUserMqDTO.getUnfollowUserId();
        LocalDateTime createTime = unfollowUserMqDTO.getCreateTime();

        // 编程式提交事务
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 取关成功需要删除数据库两条记录
                // 关注表：一条记录
                int count = followingDOMapper.deleteByUserIdAndFollowingUserId(userId, unfollowUserId);

                // 粉丝表：一条记录
                if (count > 0) {
                    fansDOMapper.deleteByUserIdAndFansUserId(unfollowUserId, userId);
                }
                return true;
            } catch (Exception ex) {
                status.setRollbackOnly(); // 标记事务为回滚
                log.error("", ex);
            }
            return false;
        }));
        // 若数据库删除成功，更新 Redis，将自己从被取注用户的 ZSet 粉丝列表删除
        if (isSuccess) {
            // 被取关用户的粉丝列表 Redis Key
            String fansRedisKey = RedisKeyConstants.buildUserFansKey(unfollowUserId);
            // 删除指定粉丝
            redisTemplate.opsForZSet().remove(fansRedisKey, userId);
        }
    }

    /**
     * 关注
     */
    private void handleFollowTagMessage(String bodyJsonStr) {

        // 将消息体JSON字符串转换为DTO对象
        FollowUserMqDTO followUserMqDTO = JsonUtils.parseObject(bodyJsonStr, FollowUserMqDTO.class);

        if (Objects.isNull(followUserMqDTO)) return;

        // 幂等性 通过联合唯一索引保证
        Long userId = followUserMqDTO.getUserId();
        Long followUserId = followUserMqDTO.getFollowUserId();
        LocalDateTime createTime = followUserMqDTO.getCreateTime();

        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try{
                // 关注成功需要往数据库插入两条数据
                // 关注表 ： 一条记录
                int count = followingDOMapper.insert(FollowingDO.builder()
                                .userId(userId)
                                .followingUserId(followUserId)
                                .createTime(createTime).build());

                // 粉丝表 ： 一条记录
                if (count > 0) {
                    fansDOMapper.insert(FansDO.builder()
                            .userId(followUserId)
                            .fansUserId(userId)
                            .createTime(createTime).build());
                }
                return true;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.error("", ex);
            }
            return false;
        }));
        log.info("## 数据库添加记录结果：{}", isSuccess);

        // 若添加数据成功 更新 Redis 中被关注用户的 ZSet 粉丝列表
        if (isSuccess) {
            // Lua 脚本
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_update_fans_zset.lua")));
            script.setResultType(Long.class);

            // 时间戳
            long timestamp = DateUtils.localDateTime2Timestamp(createTime);

            // 构建被关注用户的粉丝列表 Redis Key
            String fansRedisKey = RedisKeyConstants.buildUserFansKey(followUserId);
            // 执行脚本
            redisTemplate.execute(script, Collections.singletonList(fansRedisKey), userId, timestamp);
        }

    }
}