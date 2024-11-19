package com.arnanzz.xiaohashu.gateway.constant;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
public class RedisKeyConstants {

    /**
     * 用户对应角色集合 KEY 前缀
     */
    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";

    /**
     * 角色对应的权限集合 KEY 前缀
     */
    private static final String ROLE_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    /**
     * Sa-Token 登录的 Token KEY 前缀
     */
    public static final String SA_TOKEN_TOKEN_KEY_PREFIX = "Authorization:login:token:";

    /**
     * 构建角色对应的权限集合 KEY
     */
    public static String buildRolePermissionsKey(String roleKey) {
        return ROLE_PERMISSIONS_KEY_PREFIX + roleKey;
    }

    /**
     * 构建用户-角色 KEY
     */
    public static String buildUserRoleKey(Long userId) {
        return USER_ROLES_KEY_PREFIX + userId;
    }

}
