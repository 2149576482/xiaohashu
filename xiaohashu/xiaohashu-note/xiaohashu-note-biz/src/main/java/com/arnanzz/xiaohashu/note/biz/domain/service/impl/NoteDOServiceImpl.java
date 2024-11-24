package com.arnanzz.xiaohashu.note.biz.domain.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.arnanzz.framework.biz.context.holder.LoginUserContextHolder;
import com.arnanzz.framework.common.exception.BizException;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.framework.common.util.JsonUtils;
import com.arnanzz.xiaohashu.note.biz.constant.RedisKeyConstants;
import com.arnanzz.xiaohashu.note.biz.domain.mapper.TopicDOMapper;
import com.arnanzz.xiaohashu.note.biz.enums.NoteStatusEnum;
import com.arnanzz.xiaohashu.note.biz.enums.NoteTypeEnum;
import com.arnanzz.xiaohashu.note.biz.enums.NoteVisibleEnum;
import com.arnanzz.xiaohashu.note.biz.enums.ResponseCodeEnum;
import com.arnanzz.xiaohashu.note.biz.model.vo.FindNoteDetailReqVO;
import com.arnanzz.xiaohashu.note.biz.model.vo.FindNoteDetailRspVO;
import com.arnanzz.xiaohashu.note.biz.model.vo.PublishNoteReqVO;
import com.arnanzz.xiaohashu.note.biz.model.vo.UpdateNoteReqVO;
import com.arnanzz.xiaohashu.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.arnanzz.xiaohashu.note.biz.rpc.KeyValueRpcService;
import com.arnanzz.xiaohashu.note.biz.rpc.UserRpcService;
import com.arnanzz.xiaohashu.user.dto.resp.FindUserByIdRspDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import com.arnanzz.xiaohashu.note.biz.domain.mapper.NoteDOMapper;
import com.arnanzz.xiaohashu.note.biz.domain.entity.NoteDO;
import com.arnanzz.xiaohashu.note.biz.domain.service.NoteDOService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NoteDOServiceImpl implements NoteDOService{

    @Resource
    private NoteDOMapper noteDOMapper;
    @Resource
    private TopicDOMapper topicDOMapper;
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Resource
    private UserRpcService userRpcService;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 笔记详情本地保存
     */
    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量
            .maximumSize(10000) // 设置最大容量
            .expireAfterWrite(1, TimeUnit.HOURS) // 设置缓存条目在写入后1一个小时过期
            .build();

    /**
     * 修改笔记
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO) {

        // 笔记ID
        Long noteId = updateNoteReqVO.getId();
        // 笔记类型
        Integer type = updateNoteReqVO.getType();
        // 获取对应的枚举
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);
        // 若非图文、视频、抛出异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUris = null;
        String videoUri = null;

        switch (noteTypeEnum) {
            case IMAGE_TEXT: // 图文笔记
                List<String> imgUriList = updateNoteReqVO.getImgUris();
                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                // 校验图片数量
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");
                // TODO 将图片地址拼接成字符串 使用, 隔开
                imgUris = StringUtils.join(imgUriList, ",");
                break;
            case VIDEO: // 视频笔记
                videoUri = updateNoteReqVO.getVideoUri();
                // 进行校验
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }
        // 话题id
        Long topicId = updateNoteReqVO.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);
            // 判断提交的话题是否存在
            if (StringUtils.isBlank(topicName)) throw new BizException(ResponseCodeEnum.TOPIC_NOT_FOUND);
        }

        // 更新笔记元数据表 t_note
        String content = updateNoteReqVO.getContent();
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .isContentEmpty(StringUtils.isBlank(content))
                .imgUris(imgUris)
                .videoUri(videoUri)
                .title(updateNoteReqVO.getTitle())
                .topicId(topicId)
                .topicName(topicName)
                .type(type)
                .updateTime(LocalDateTime.now())
                .build();
        noteDOMapper.updateByPrimaryKey(noteDO);

        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);

        // 删除本地缓存
        LOCAL_CACHE.invalidate(noteId);

        // 笔记内容更新
        // 查询此篇笔记对应的uuid
        NoteDO noteDO1 = noteDOMapper.selectByPrimaryKey(noteId);
        String contentUuid = noteDO1.getContentUuid();

        // 笔记内容是否更新成功
        boolean updateNoteContentSuccess = false;

        if (StringUtils.isNotBlank(content)) {
            // 若笔记内容为空 则删除 相对应 kv存储服务中的笔记内容
            updateNoteContentSuccess = keyValueRpcService.deleteNoteContent(contentUuid);
        } else {
            // 调用kv 服务 更新 短文本
            updateNoteContentSuccess = keyValueRpcService.addNoteContent(contentUuid, content);
        }

        // 如果更新失败 抛出异常
        if (!updateNoteContentSuccess) {
            throw new BizException(ResponseCodeEnum.NOTE_UPDATE_FAIL);
        }
        return Response.success();
    }

    /**
     * 笔记详情页面
     */
    @Override
    @SneakyThrows
    public Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO) {
        // 查询的笔记 ID
        Long noteId = findNoteDetailReqVO.getId();

        LOCAL_CACHE.invalidate(noteId);

        // 当前登录的用户
        Long userId = LoginUserContextHolder.getUserId();

        // 从本地缓存中查询数据
        String findNoteDetailRspVOStrLocalCache = LOCAL_CACHE.getIfPresent(noteId);
        if (StringUtils.isNotBlank(findNoteDetailRspVOStrLocalCache)) {
            // 如果不为空  将字符串转换为对象
            FindNoteDetailRspVO findNoteDetailRspVO = JsonUtils.parseObject(findNoteDetailRspVOStrLocalCache, FindNoteDetailRspVO.class);
            log.info("==> 命中了本地缓存；{}", findNoteDetailRspVOStrLocalCache);
            // 可见性校验
            checkNoteVisibleFromVO(userId, findNoteDetailRspVO);
            return Response.success(findNoteDetailRspVO);
        }

        // 先从Redis中获取笔记数据
        String noteDetailKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        String noteDetailValue = redisTemplate.opsForValue().get(noteDetailKey);

        // 若缓存中查询到笔记详情 直接返回
        if (StringUtils.isNotBlank(noteDetailValue)) {
            FindNoteDetailRspVO findNoteDetailRspVO = JsonUtils.parseObject(noteDetailValue, FindNoteDetailRspVO.class);
            // 异步写入 本地缓存
            threadPoolTaskExecutor.submit(() -> {
                LOCAL_CACHE.put(noteId, Objects.isNull(findNoteDetailRspVO) ? "null" : JsonUtils.toJsonString(findNoteDetailRspVO));
            });
            checkNoteVisibleFromVO(userId, findNoteDetailRspVO);
            return Response.success(findNoteDetailRspVO);
        }

        // 若redis查询不到 走数据库
        // 查询笔记
        NoteDO noteDO = noteDOMapper.selectByPrimaryKey(noteId);

        // 若笔记不存在 抛出业务异常
        if (Objects.isNull(noteDO)) {
            threadPoolTaskExecutor.execute(() -> {
                // 防止缓存穿透 将空数据存入redis缓存
                // 保底1分钟
                long expireSeconds = 60 + RandomUtil.randomInt(60);
                redisTemplate.opsForValue().set(noteDetailKey, "null", expireSeconds, TimeUnit.SECONDS);
            });
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 可见性校验
        Integer visible = noteDO.getVisible();
        checkNoteVisible(visible, userId, noteDO.getCreatorId());

        // 并发优化 指定线程池
        // RPC 调用用户服务
        Long creatorId = noteDO.getCreatorId();
        CompletableFuture<FindUserByIdRspDTO> userResultFuture = CompletableFuture
                .supplyAsync(() -> userRpcService.findById(creatorId), threadPoolTaskExecutor);

        // RPC 调用KV 存储服务获取内容
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        if (Objects.equals(noteDO.getIsContentEmpty(), Boolean.FALSE)) {
            contentResultFuture = CompletableFuture
                    .supplyAsync(() -> keyValueRpcService.findNoteContent(noteDO.getContentUuid()));
        }

        CompletableFuture<String> finalContentResultFuture = contentResultFuture;
        CompletableFuture<FindNoteDetailRspVO> resultFuture = CompletableFuture
                // allOf 等待所有异步任务完成 会返回一个新的CompletableFuture 通过thenApply 处理结果
                .allOf(userResultFuture, contentResultFuture)
                .thenApply(s -> {
                    // 获取 Future 返回的结果
                    FindUserByIdRspDTO findUserByIdRspDTO = userResultFuture.join();
                    String content = finalContentResultFuture.join();

                    // 笔记类型
                    Integer type = noteDO.getType();
                    // 图文笔记图片链接 字符串
                    String imgUris = noteDO.getImgUris();
                    // 图文笔记图片链接 集合
                    List<String> imgUrisList = null;
                    // 如果是查询的是图文笔记 需要将图片链接的逗号分开 转换成集合
                    if (Objects.equals(type, NoteTypeEnum.IMAGE_TEXT.getCode()) && StringUtils.isNotBlank(imgUris)) {
                        // TODO 将字符串转换成List集合 按照逗号,切割
                        imgUrisList = List.of(imgUris.split(","));
                    }

                    // 构建返参VO
                    return FindNoteDetailRspVO.builder()
                            .id(noteDO.getId())
                            .type(noteDO.getType())
                            .title(noteDO.getTitle())
                            .content(content)
                            .imgUris(imgUrisList)
                            .topicId(noteDO.getTopicId())
                            .topicName(noteDO.getTopicName())
                            .creatorId(noteDO.getCreatorId())
                            .creatorName(findUserByIdRspDTO.getNickname())
                            .avatar(findUserByIdRspDTO.getAvatar())
                            .videoUri(noteDO.getVideoUri())
                            .updateTime(noteDO.getUpdateTime())
                            .visible(noteDO.getVisible())
                            .build();
                });

        FindNoteDetailRspVO findNoteDetailRspVO = resultFuture.get();
        // 异步线程 将笔记详情存入redis
        threadPoolTaskExecutor.submit(() -> {
            String jsonString = JsonUtils.toJsonString(findNoteDetailRspVO);
            //过期时间 随机 保底1天
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            redisTemplate.opsForValue().set(noteDetailKey, jsonString, expireSeconds, TimeUnit.SECONDS);
        });
        return Response.success(findNoteDetailRspVO);
    }

    /**
     * 可见性校验
     */
    private void checkNoteVisible(Integer visible, Long userId, Long creatorId) {
        if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode())
                && !Objects.equals(userId, creatorId)) { // 仅自己可见
            throw new BizException(ResponseCodeEnum.NOTE_PRIVATE);
        }
    }

    /**
     * 校验笔记的可见性（针对 VO 实体类）
     */
    private void checkNoteVisibleFromVO(Long userId, FindNoteDetailRspVO findNoteDetailRspVO) {
        if (Objects.nonNull(findNoteDetailRspVO)) {
            Integer visible = findNoteDetailRspVO.getVisible();
            checkNoteVisible(visible, userId, findNoteDetailRspVO.getCreatorId());
        }
    }

    /**
     * 笔记发布
     */
    @Override
    public Response<?> publishNote(PublishNoteReqVO publishNoteReqVO) {

        // 笔记类型
        Integer type = publishNoteReqVO.getType();

        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

        // 若非图文 视频抛出异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUris = null;
        // 笔记内容是否为空，默认值为 true，即空
        Boolean isContentEmpty = true;
        String videoUri = null;

        switch (noteTypeEnum) {
            case IMAGE_TEXT: // 图文笔记
                List<String> imgUriList = publishNoteReqVO.getImgUris();
                // 校验图片
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                // 校验图片数量
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于8张");
                // 将图片链接拼接 以逗号分开
                imgUris = StringUtils.join(imgUriList, ",");
                break;
            case VIDEO: // 视频笔记
                videoUri = publishNoteReqVO.getVideoUri();
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }

        // RPC 调用 生成笔记ID
        String snowflakeId = distributedIdGeneratorRpcService.getSnowflakeId();
        // 笔记内容UUID
        String contentUuid = null;
        // 笔记内容
        String content = publishNoteReqVO.getContent();
        // 若用户填写了笔记内容
        if (StringUtils.isNotBlank(content)) {
            // 如果内容不为空 则false
            isContentEmpty = false;
            contentUuid = UUID.randomUUID().toString();
            // RPC 调用 添加笔记内容
            boolean isSaveResult = keyValueRpcService.addNoteContent(contentUuid, content);
            // 若添加失败 抛出异常
            if (!isSaveResult) {
                throw new BizException(ResponseCodeEnum.NOTE_PUBLISH_FAIL);
            }
        }

        // 话题
        Long topicId = publishNoteReqVO.getTopicId();
        String topicName = null;
        // 如果id 不为空 获取话题名称
        if (Objects.nonNull(topicId)) {
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);
        }

        // 发布者ID
        Long userId = LoginUserContextHolder.getUserId();

        // 构建笔记对象
        NoteDO noteDO = NoteDO.builder()
                .id(Long.valueOf(snowflakeId))
                .title(publishNoteReqVO.getTitle())
                .isContentEmpty(isContentEmpty)
                .creatorId(userId)
                .topicId(topicId)
                .topicName(topicName)
                .isTop(Boolean.FALSE)
                .type(type)
                .imgUris(imgUris)
                .videoUri(videoUri)
                .visible(NoteVisibleEnum.PUBLIC.getCode())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .status(NoteStatusEnum.BE_EXAMINE.getCode())
                .contentUuid(contentUuid)
                .build();

        try {
            // 笔记入库存储
            noteDOMapper.insertSelective(noteDO);
        } catch (Exception e) {
            log.error("==> 笔记存储失败", e);

            // 笔记保存失败 删除笔记内容
            if (StringUtils.isNotBlank(contentUuid)) {
                keyValueRpcService.deleteNoteContent(contentUuid);
            }
        }
        return Response.success();
    }
}
