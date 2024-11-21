package com.arnanzz.xiaohashu.note.biz.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 话题表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopicDO {
    /**
    * 主键ID
    */
    private Long id;

    /**
    * 话题名称
    */
    private String name;

    /**
    * 创建时间
    */
    private LocalDateTime createTime;

    /**
    * 更新时间
    */
    private LocalDateTime updateTime;

    /**
    * 逻辑删除(0：未删除 1：已删除)
    */
    private Boolean isDeleted;
}