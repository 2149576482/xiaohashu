package com.arnanzz.xiaohashu.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import com.arnanzz.xiaohashu.gateway.constant.RedisKeyConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 自定义权限验证接口扩展
 * 获取当前登录账号的权限码集合
 **/
@Component
@Slf4j
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取当前登录用户的权限
     */
    @Override
    @SneakyThrows
    public List<String> getPermissionList(Object loginId, String loginType) {
        log.info("## 获取用户权限列表, loginId: {}", loginId);

        // 构建用户-角色 Redis key
        String redisRoleKey = RedisKeyConstants.buildUserRoleKey(Long.valueOf(loginId.toString()));

        // 获取该用户角色集合
        String useRolesValue = (String) redisTemplate.opsForValue().get(redisRoleKey);

        if (StringUtils.isBlank(useRolesValue)) {
            return null;
        }

        // 将角色集合 转换成 List<String> 集合
        List<String> userRoleKeys = objectMapper.readValue(useRolesValue, new TypeReference<>() {});

        if (CollUtil.isNotEmpty(userRoleKeys)) {
            // 构建角色-权限 Redis Key 集合
            List<String> rolePermissionsKeys = userRoleKeys.stream().map(RedisKeyConstants::buildRolePermissionsKey)
                    .toList();
            // 通过key集合 批量查询权限
            List<Object> rolePermissionsValues = redisTemplate.opsForValue().multiGet(rolePermissionsKeys);

            if (CollUtil.isNotEmpty(rolePermissionsValues)) {
                List<String> permissions = CollUtil.newArrayList();

                // 遍历所有的权限集合
                rolePermissionsValues.forEach(json -> {
                    try {
                        // 将权限集合 转换成 List<String> 集合
                        List<String> permissionDOList = objectMapper.readValue(json.toString(), new TypeReference<>() {});
                        permissions.addAll(permissionDOList);
                    } catch (JsonProcessingException e) {
                        log.error("==> JSON 解析错误: ", e);
                    }
                });
                return permissions;
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户的角色
     */
    @Override
    @SneakyThrows
    public List<String> getRoleList(Object loginId, String loginType) {
        log.info("## 获取用户角色列表, loginId: {}", loginId);

        // 构建用户-角色 Redis key
        String redisKey = RedisKeyConstants.buildUserRoleKey(Long.valueOf(String.valueOf(loginId)));

        // 根据用户 ID ，从 Redis 中获取该用户的角色集合
        String useRolesValue  = (String) redisTemplate.opsForValue().get(redisKey);

        if (StringUtils.isBlank(useRolesValue)) {
            return null;
        }
        return objectMapper.readValue(useRolesValue, new TypeReference<>() {});
    }
}
