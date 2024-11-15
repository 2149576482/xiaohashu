package com.arnanzz.xiaohashu.user.biz.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户角色表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRoleRelDO {
    /**
    * 主键ID
    */
    private Long id;

    /**
    * 用户ID
    */
    private Long userId;

    /**
    * 角色ID
    */
    private Long roleId;

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