package com.arnanzz.xiaohashu.user.relation.biz.domain.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.arnanzz.framework.biz.context.holder.LoginUserContextHolder;
import com.arnanzz.framework.common.exception.BizException;
import com.arnanzz.framework.common.response.PageResponse;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.framework.common.util.DateUtils;
import com.arnanzz.framework.common.util.JsonUtils;
import com.arnanzz.xiaohashu.user.dto.resp.FindUserByIdRspDTO;
import com.arnanzz.xiaohashu.user.relation.biz.constant.MQConstants;
import com.arnanzz.xiaohashu.user.relation.biz.constant.RedisKeyConstants;
import com.arnanzz.xiaohashu.user.relation.biz.domain.entity.FansDO;
import com.arnanzz.xiaohashu.user.relation.biz.domain.entity.FollowingDO;
import com.arnanzz.xiaohashu.user.relation.biz.domain.mapper.FansDOMapper;
import com.arnanzz.xiaohashu.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.arnanzz.xiaohashu.user.relation.biz.domain.service.RelationService;
import com.arnanzz.xiaohashu.user.relation.biz.enums.LuaResultEnum;
import com.arnanzz.xiaohashu.user.relation.biz.enums.ResponseCodeEnum;
import com.arnanzz.xiaohashu.user.relation.biz.model.dto.FollowUserMqDTO;
import com.arnanzz.xiaohashu.user.relation.biz.model.dto.UnfollowUserMqDTO;
import com.arnanzz.xiaohashu.user.relation.biz.model.vo.*;
import com.arnanzz.xiaohashu.user.relation.biz.rpc.UserRpcService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 关系服务
 **/
@Service
@Slf4j
public class RelationServiceImpl implements RelationService {

    @Resource
    private UserRpcService userRpcService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private FollowingDOMapper followingDOMapper;

    @Resource
    private FansDOMapper fansDOMapper;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    /**
     * 查询粉丝列表
     */
    @Override
    public PageResponse<FindFansUserRspVO> findFansList(FindFansListReqVO findFansListReqVO) {

        // 想要查询用户 ID
        Long userId = findFansListReqVO.getUserId();

        // 页码
        Integer pageNo = findFansListReqVO.getPageNo();

        // 先查询Redis
        String fansListRedisKey = RedisKeyConstants.buildUserFansKey(userId);

        // 查询目标用户粉丝列表ZSet集合的大小
        Long total = redisTemplate.opsForZSet().zCard(fansListRedisKey);

        // 返参
        List<FindFansUserRspVO> findFansUserRspVOS = null;

        // 每页展示10条数据
        long limit = 10;

        if (total > 0) {
            // 计算一共多少页
            long offset = PageResponse.getTotalPage(total, limit);

            // 使用 ZREVRANGEBYSCORE 命令按 score 降序获取元素，同时使用 LIMIT 子句实现分页
            Set<Object> fansUserIdsSet = redisTemplate.opsForZSet()
                    .reverseRangeByScore(fansListRedisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, offset, limit);

            if (CollUtil.isNotEmpty(fansUserIdsSet)) {
                // 提取用户 ID 到集合中
                List<Long> userIds = fansUserIdsSet.stream().map(object -> Long.valueOf(object.toString())).toList();

                // RPC 批量查询用户信息
                findFansUserRspVOS = rpcUserServiceAndCountServiceAndDTO2VO(userIds, findFansUserRspVOS);
            }
        } else {
            // 若redis 中无数据 查询数据库
            total = fansDOMapper.selectCountByUserId(userId);

            // 计算一共多少页
            long totalPage = PageResponse.getTotalPage(total, limit);

            // 请求的页码超出了总页数（只允许查询前 500 页）
            if (pageNo > 500 || pageNo > totalPage) return PageResponse.success(null, pageNo, total);

            // 偏移量
            long offset = PageResponse.getOffset(pageNo, limit);

            // 分页查询
            List<FansDO> fansDOS = fansDOMapper.selectPageListByUserId(userId, offset, limit);

            // 若记录不为空
            if (CollUtil.isNotEmpty(fansDOS)) {
                // 提取所有粉丝用户 ID 到集合中
                List<Long> userIds = fansDOS.stream().map(FansDO::getFansUserId).toList();

                // RPC: 调用用户服务、计数服务，并将 DTO 转换为 VO
                findFansUserRspVOS = rpcUserServiceAndCountServiceAndDTO2VO(userIds, findFansUserRspVOS);

                // 异步将粉丝列表同步到 Redis（最多5000条）
                threadPoolTaskExecutor.submit(() -> syncFansList2Redis(userId));
            }
        }
        return PageResponse.success(findFansUserRspVOS, pageNo, total);
    }

    /**
     * 粉丝列表同步到 Redis（最多5000条）
     */
    private void syncFansList2Redis(Long userId) {
        // 查询粉丝列表（最多5000位用户）
        List<FansDO> fansDOS = fansDOMapper.select5000FansByUserId(userId);
        if (CollUtil.isNotEmpty(fansDOS)) {
            // 用户粉丝列表 Redis Key
            String fansListRedisKey = RedisKeyConstants.buildUserFansKey(userId);
            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            // 构建 Lua 参数
            Object[] luaArgs = buildFansZSetLuaArgs(fansDOS, expireSeconds);

            // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(fansListRedisKey), luaArgs);
        }
    }

    /**
     * 构建 Lua 脚本参数：粉丝列表
     */
    private static Object[] buildFansZSetLuaArgs(List<FansDO> fansDOS, long expireSeconds) {
        int argsLength = fansDOS.size() * 2 + 1; // 每个粉丝关系有 2 个参数（score 和 value），再加一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (FansDO fansDO : fansDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(fansDO.getCreateTime()); // 粉丝的关注时间作为 score
            luaArgs[i + 1] = fansDO.getFansUserId();          // 粉丝的用户 ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }

    /**
     * 查询用户关注列表
     */
    @Override
    public PageResponse<FindFollowingUserRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO) {
        // 想要查询用户 ID
        Long userId = findFollowingListReqVO.getUserId();
        // 页码
        Integer pageNo = findFollowingListReqVO.getPageNo();

        // 先从Redis中查询
        String followingListRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);

        // 查询目标用户关注列表ZSet集合的大小
        Long total = redisTemplate.opsForZSet().zCard(followingListRedisKey);

        // 返参
        List<FindFollowingUserRspVO> findFollowingUserRspVOS = null;

        // 每页展示10条数据
        long limit = 10;

        if (total > 0) {
            // 计算一共多少页
            long totalPage = PageResponse.getTotalPage(total, limit);

            // 请求的页码超出了总页数
            if (pageNo > totalPage) return PageResponse.success(null, pageNo, total);

            // 准备从 Redis 中查询 ZSet 分页数据
            // 每页 10 个元素，计算偏移量
            long offset = PageResponse.getOffset(pageNo, limit);

            // 使用 ZREVRANGEBYSCORE 命令按 score 降序获取元素，同时使用 LIMIT 子句实现分页
            // 注意：这里使用了 Double.POSITIVE_INFINITY 和 Double.NEGATIVE_INFINITY 作为分数范围
            // 因为关注列表最多有 1000 个元素，这样可以确保获取到所有的元素
            Set<Object> followingUserIdsSet = redisTemplate.opsForZSet()
                    .reverseRangeByScore(followingListRedisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, offset, limit);

            if (CollUtil.isNotEmpty(followingUserIdsSet)) {
                // 提取所有用户的 ID 到集合中
                List<Long> userIds = followingUserIdsSet.stream().map(object -> Long.valueOf(object.toString())).toList();

                // 若不为空 DTO 转 VO
                findFollowingUserRspVOS = rpcUserServiceAndDTO2VO(userIds, findFollowingUserRspVOS);
            }
        } else {
            // 若redis中 没有数据 则去数据库中查询
            // 先查询总量
            long count = followingDOMapper.selectCountByUserId(userId);

            // 计算多少页
            long totalPage = PageResponse.getTotalPage(count, limit);

            // 请求的页码超出了总页数
            if (pageNo > totalPage) return PageResponse.success(null, pageNo, total);

            // 偏移量
            long offset = PageResponse.getOffset(pageNo, limit);

            // 分页查询
            List<FollowingDO> followingDOS = followingDOMapper.selectPageListByUserId(userId, offset, limit);

            // 若不为空
            if (CollUtil.isNotEmpty(followingDOS)) {
                // 提取所有关注用户 ID 到集合中
                List<Long> userIds = followingDOS.stream().map(FollowingDO::getFollowingUserId).toList();
                // RPC 批量查询用户信息
                findFollowingUserRspVOS = rpcUserServiceAndDTO2VO(userIds, findFollowingUserRspVOS);

               // 异步将关注信息 刷新到redis中
                syncFollowingList2Redis(userId);
            }
        }
        return PageResponse.success(findFollowingUserRspVOS, pageNo, total);
    }

    /**
     * 全量同步关注列表至 Redis 中
     */
    private void syncFollowingList2Redis(Long userId) {
        // 查询全量关注用户列表（1000位用户）
        List<FollowingDO> followingDOS = followingDOMapper.selectAllByUserId(userId);
        if (CollUtil.isNotEmpty(followingDOS)) {
            // 用户关注列表 Redis Key
            String followingListRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);
            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            // 构建 Lua 参数
            Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);

            // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(followingListRedisKey), luaArgs);
        }
    }

    /**
     * RPC: 调用用户服务、计数服务，并将 DTO 转换为 VO 粉丝列表
     */
    private List<FindFansUserRspVO> rpcUserServiceAndCountServiceAndDTO2VO(List<Long> userIds, List<FindFansUserRspVO> findFansUserRspVOS) {
        // RPC: 批量查询用户信息
        List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);

        // TODO RPC: 批量查询用户的计数数据（笔记总数、粉丝总数）

        // 若不为空，DTO 转 VO
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            findFansUserRspVOS = findUserByIdRspDTOS.stream()
                    .map(dto -> FindFansUserRspVO.builder()
                            .userId(dto.getId())
                            .avatar(dto.getAvatar())
                            .nickname(dto.getNickname())
                            .noteTotal(0L) // TODO: 这块的数据暂无，后续补充
                            .fansTotal(0L) // TODO: 这块的数据暂无，后续补充
                            .build())
                    .toList();
        }
        return findFansUserRspVOS;
    }

    /**
     * RPC: 调用用户服务，并将 DTO 转换为 VO
     */
    private List<FindFollowingUserRspVO> rpcUserServiceAndDTO2VO(List<Long> userIds, List<FindFollowingUserRspVO> findFollowingUserRspVOS) {
        // RPC: 批量查询用户信息
        List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);

        // 若不为空，DTO 转 VO
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            findFollowingUserRspVOS = findUserByIdRspDTOS.stream()
                    .map(dto -> FindFollowingUserRspVO.builder()
                            .userId(dto.getId())
                            .avatar(dto.getAvatar())
                            .nickname(dto.getNickname())
                            .introduction(dto.getIntroduction())
                            .build())
                    .toList();
        }
        return findFollowingUserRspVOS;
    }

    /**
     * 取关用户
     */
    @Override
    public Response<?> unfollow(UnFollowUserReqVO unfollowUserReqVO) {
        // 想要取关的用户
        Long unFollowUserId = unfollowUserReqVO.getFollowUserId();
        // 当前登录用户
        Long userId = LoginUserContextHolder.getUserId();

        // 无法取关自己
        if (Objects.equals(unFollowUserId, userId)) {
            throw new BizException(ResponseCodeEnum.CANT_UNFOLLOW_YOUR_SELF);
        }

        // 校验关注的用户是否存在
        FindUserByIdRspDTO findUserByIdRspDTO = userRpcService.findById(unFollowUserId);
        if (Objects.isNull(findUserByIdRspDTO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXISTED);
        }

        // 当前用户的关注列表 Redis Key
        String followingRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/unfollow_check_and_delete.lua")));
        // 返回值类型
        script.setResultType(Long.class);
        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(followingRedisKey), unFollowUserId);

        // 校验 Lua 脚本执行结果
        // 取关的用户不在关注列表中
        if (Objects.equals(result, LuaResultEnum.NOT_FOLLOWED.getCode())) {
            throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
        }

        if (Objects.equals(result, LuaResultEnum.ZSET_NOT_EXIST.getCode())) { // ZSET 关注列表不存在
            // 从数据库查询当前用户的关注关系记录
            List<FollowingDO> followingDOS = followingDOMapper.selectByUserId(userId);

            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);

            // 若记录为空，则表示还未关注任何人，提示还未关注对方
            if (CollUtil.isEmpty(followingDOS)) {
                throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
            } else { // 若记录不为空，则将关注关系数据全量同步到 Redis 中，并设置过期时间；
                // 构建 Lua 参数
                Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);

                // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
                DefaultRedisScript<Long> script3 = new DefaultRedisScript<>();
                script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                script3.setResultType(Long.class);
                redisTemplate.execute(script3, Collections.singletonList(followingRedisKey), luaArgs);

                // 再次调用上面的 Lua 脚本：unfollow_check_and_delete.lua , 将取关的用户删除
                result = redisTemplate.execute(script, Collections.singletonList(followingRedisKey), unFollowUserId);
                // 再次校验结果
                if (Objects.equals(result, LuaResultEnum.NOT_FOLLOWED.getCode())) {
                    throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
                }
            }
        }

        // 发送MQ
        // 构建消息体MQ
        UnfollowUserMqDTO unfollowUserMqDTO = UnfollowUserMqDTO.builder()
                .userId(userId)
                .unfollowUserId(unFollowUserId)
                .createTime(LocalDateTime.now())
                .build();

        // 构建消息对象 转换成JSON
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(unfollowUserMqDTO)).build();

        // 通过冒号连接 可以让MQ发送给主题topic 时， 携带上tag
        String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" + MQConstants.TAG_UNFOLLOW;

        log.info("==> 开始发送取关操作 MQ, 消息体: {}", unfollowUserMqDTO);

        String hashKey = String.valueOf(userId);
        // 异步发送消息 提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> MQ 发送异常: ", throwable);
            }
        });
        return Response.success();
    }

    /**
     * 关注用户
     */
    @Override
    @SneakyThrows
    public Response<?> follow(FollowUserReqVO followUserReqVO) {

        // 关注的用户ID
        Long followUserId = followUserReqVO.getFollowUserId();

        // 当前用户ID
        Long userId = LoginUserContextHolder.getUserId();

        // 校验关注的用户是否是自己
        if (Objects.equals(userId, followUserId)) {
            throw new BizException(ResponseCodeEnum.CANT_FOLLOW_YOUR_SELF);
        }

        // 校验关注的用户是否存在
        FindUserByIdRspDTO byId = userRpcService.findById(followUserId);
        if (Objects.isNull(byId)) {
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }

        // 构建用户关注列表的 Key
        String followingRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_add.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        LocalDateTime now = LocalDateTime.now();
        // 当前时间戳
        long timestamp = DateUtils.localDateTime2Timestamp(now);

        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(followingRedisKey), followUserId, timestamp);

        // 校验 Lua 脚本执行结果
        checkLuaScriptResult(result);

        // ZSET 不存在
        if (Objects.equals(result, LuaResultEnum.ZSET_NOT_EXIST.getCode())) {
            // 从数据库中查询当前用户的关注关系记录
            List<FollowingDO> followingDOS = followingDOMapper.selectByUserId(userId);

            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

            // 若记录为空 直接ZADD 关系数据 并设置过期时间
            if (CollUtil.isEmpty(followingDOS)) {
                DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
                script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_add_and_expire.lua")));
                script2.setResultType(Long.class);

                // TODO: 可以根据用户类型，设置不同的过期时间，若当前用户为大V, 则可以过期时间设置的长些或者不设置过期时间；如不是，则设置的短些
                // 可以从计数服务获取用户的粉丝数，目前计数服务还没创建，则暂时采用统一的过期策略
                redisTemplate.execute(script2, Collections.singletonList(followingRedisKey), followUserId, timestamp, expireSeconds);
            } else { // 若记录不为空 则将关注关系数据全量同步到Redis中 并设置过期时间
                // 构建 Lua 参数
                Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);

                // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
                DefaultRedisScript<Long> script3 = new DefaultRedisScript<>();
                script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                script3.setResultType(Long.class);
                redisTemplate.execute(script3, Collections.singletonList(followingRedisKey), luaArgs);

                // 再次调用上面的 Lua 脚本：follow_check_and_add.lua , 将最新的关注关系添加进去
                result = redisTemplate.execute(script, Collections.singletonList(followingRedisKey), followUserId, timestamp);
                checkLuaScriptResult(result);
            }
        }
        // 发送 MQ
        // 构建消息体 DTO
        FollowUserMqDTO followUserMqDTO = FollowUserMqDTO.builder()
                .userId(userId)
                .followUserId(followUserId)
                .createTime(now)
                .build();

        // 构建消息对象 并将DTO 转换成 Json字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(followUserMqDTO)).build();

        // 通过冒号连接 可让MQ 发送给主题Topic时，携带上标签tag
        String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" + MQConstants.TAG_FOLLOW;
        log.info("==> 开始发送关注操作MQ，消息体:{}", followUserMqDTO);

        String hashKey = String.valueOf(userId);
        // 异步发送MQ消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> MQ 发送异常: ", throwable);
            }
        });
        return Response.success();
    }

    /**
     * 校验 Lua 脚本结果，根据状态码抛出对应的业务异常
     */
    private static void checkLuaScriptResult(Long result) {
        LuaResultEnum luaResultEnum = LuaResultEnum.valueOf(result);

        if (Objects.isNull(luaResultEnum)) throw new RuntimeException("Lua 返回结果错误");
        // 校验 Lua 脚本执行结果
        switch (luaResultEnum) {
            // 关注数已达到上限
            case FOLLOW_LIMIT -> throw new BizException(ResponseCodeEnum.FOLLOWING_COUNT_LIMIT);
            // 已经关注了该用户
            case ALREADY_FOLLOWED -> throw new BizException(ResponseCodeEnum.ALREADY_FOLLOWED);
        }
    }

    /**
     * 构建 Lua 脚本参数
     */
    private static Object[] buildLuaArgs(List<FollowingDO> followingDOS, long expireSeconds) {
        int argsLength = followingDOS.size() * 2 + 1; // 每个关注关系有 2 个参数（score 和 value），再加一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (FollowingDO following : followingDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(following.getCreateTime()); // 关注时间作为 score
            luaArgs[i + 1] = following.getFollowingUserId();          // 关注的用户 ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }
}
