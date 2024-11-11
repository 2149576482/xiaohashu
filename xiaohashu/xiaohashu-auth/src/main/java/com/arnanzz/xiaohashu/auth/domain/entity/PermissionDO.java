package com.arnanzz.xiaohashu.auth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 权限表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionDO {
    /**
    * 主键ID
    */
    private Long id;

    /**
    * 父ID
    */
    private Long parentId;

    /**
    * 权限名称
    */
    private String name;

    /**
    * 类型(1：目录 2：菜单 3：按钮)
    */
    private Integer type;

    /**
    * 菜单路由
    */
    private String menuUrl;

    /**
    * 菜单图标
    */
    private String menuIcon;

    /**
    * 管理系统中的显示顺序
    */
    private Integer sort;

    /**
    * 权限标识
    */
    private String permissionKey;

    /**
    * 状态(0：启用；1：禁用)
    */
    private Integer status;

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