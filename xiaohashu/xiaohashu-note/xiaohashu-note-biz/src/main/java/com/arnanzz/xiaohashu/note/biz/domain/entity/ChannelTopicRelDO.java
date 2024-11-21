package com.arnanzz.xiaohashu.note.biz.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 频道-话题关联表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelTopicRelDO {
    /**
    * 主键ID
    */
    private Long id;

    /**
    * 频道ID
    */
    private Long channelId;

    /**
    * 话题ID
    */
    private Long topicId;

    /**
    * 创建时间
    */
    private LocalDateTime createTime;

    /**
    * 更新时间
    */
    private LocalDateTime updateTime;
}