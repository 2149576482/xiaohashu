package com.arnanzz.xiaohashu.note.biz.domain.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.arnanzz.framework.biz.context.holder.LoginUserContextHolder;
import com.arnanzz.framework.common.exception.BizException;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.note.biz.domain.mapper.TopicDOMapper;
import com.arnanzz.xiaohashu.note.biz.enums.NoteStatusEnum;
import com.arnanzz.xiaohashu.note.biz.enums.NoteTypeEnum;
import com.arnanzz.xiaohashu.note.biz.enums.NoteVisibleEnum;
import com.arnanzz.xiaohashu.note.biz.enums.ResponseCodeEnum;
import com.arnanzz.xiaohashu.note.biz.model.vo.PublishNoteReqVO;
import com.arnanzz.xiaohashu.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.arnanzz.xiaohashu.note.biz.rpc.KeyValueRpcService;
import com.google.common.base.Preconditions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import com.arnanzz.xiaohashu.note.biz.domain.mapper.NoteDOMapper;
import com.arnanzz.xiaohashu.note.biz.domain.entity.NoteDO;
import com.arnanzz.xiaohashu.note.biz.domain.service.NoteDOService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class NoteDOServiceImpl implements NoteDOService{

    @Autowired
    private NoteDOMapper noteDOMapper;

    @Resource
    private TopicDOMapper topicDOMapper;
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource
    private KeyValueRpcService keyValueRpcService;

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
