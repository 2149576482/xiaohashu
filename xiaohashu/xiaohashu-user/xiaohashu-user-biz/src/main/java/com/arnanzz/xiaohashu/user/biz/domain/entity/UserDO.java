package com.arnanzz.xiaohashu.user.biz.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 用户表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDO {
    /**
    * 主键ID
    */
    private Long id;

    /**
    * 小哈书号(唯一凭证)
    */
    private String xiaohashuId;

    /**
    * 密码
    */
    private String password;

    /**
    * 昵称
    */
    private String nickname;

    /**
    * 头像
    */
    private String avatar;

    /**
    * 生日
    */
    private LocalDate birthday;

    /**
    * 背景图
    */
    private String backgroundImg;

    /**
    * 手机号
    */
    private String phone;

    /**
    * 性别(0：女 1：男)
    */
    private Integer sex;

    /**
    * 状态(0：启用 1：禁用)
    */
    private Integer status;

    /**
    * 个人简介
    */
    private String introduction;

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