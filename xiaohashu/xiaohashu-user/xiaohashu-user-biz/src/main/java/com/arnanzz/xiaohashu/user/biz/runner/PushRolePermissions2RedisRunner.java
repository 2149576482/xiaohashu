package com.arnanzz.xiaohashu.user.biz.runner;

import cn.hutool.core.collection.CollUtil;
import com.arnanzz.framework.common.util.JsonUtils;
import com.arnanzz.xiaohashu.user.biz.constant.RedisKeyConstants;
import com.arnanzz.xiaohashu.user.biz.domain.entity.PermissionDO;
import com.arnanzz.xiaohashu.user.biz.domain.entity.RoleDO;
import com.arnanzz.xiaohashu.user.biz.domain.entity.RolePermissionRelDO;
import com.arnanzz.xiaohashu.user.biz.domain.mapper.PermissionDOMapper;
import com.arnanzz.xiaohashu.user.biz.domain.mapper.RoleDOMapper;
import com.arnanzz.xiaohashu.user.biz.domain.mapper.RolePermissionRelDOMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
@Component
@Slf4j
public class PushRolePermissions2RedisRunner implements ApplicationRunner {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private PermissionDOMapper permissionDOMapper;
    @Resource
    private RolePermissionRelDOMapper rolePermissionRelDOMapper;

    // 权限同步标记 Key
    private static final String PUSH_PERMISSION_FLAG = "push.permission.flag";

    @Override
    public void run(ApplicationArguments args) {
        log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");

        try {

            // 是否能够同步数据: 原子操作，只有在键 PUSH_PERMISSION_FLAG 不存在时，才会设置该键的值为 "1"，并设置过期时间为 1 天
            boolean canPushed = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(PUSH_PERMISSION_FLAG, "1", 1, TimeUnit.DAYS));

            // 如果无法同步权限数据
            if (!canPushed) {
                log.warn("==> 角色权限数据已经同步至 Redis 中，不再同步...");
                return;
            }

            // 查询出 所有的角色
            List<RoleDO> roleDOS = roleDOMapper.selectEnabledList();

            // 如果角色不为空
            if (CollUtil.isNotEmpty(roleDOS)) {
                // 获取到所有的角色id
                List<Long> roleIds = roleDOS.stream().map(RoleDO::getId).toList();
                // 根据角色id 查询出所有的角色权限关联信息
                List<RolePermissionRelDO> rolePermissionRelDOS = rolePermissionRelDOMapper.selectByRoleIds(roleIds);

                // 按照角色 id 进行分组 每个角色id 对应多个权限
                Map<Long, List<Long>> roleIdPermissionIdsMap = rolePermissionRelDOS.stream().collect(
                        Collectors.groupingBy(RolePermissionRelDO::getRoleId,
                                Collectors.mapping(RolePermissionRelDO::getPermissionId, Collectors.toList()))
                );

                // 查询APP所有被启用的权限
                List<PermissionDO> permissionDOS = permissionDOMapper.selectAppEnabledList();

                // 权限id 权限do
                Map<Long, PermissionDO> permissionDOMap = permissionDOS.stream().collect(
                        Collectors.toMap(PermissionDO::getId, permissionDO -> permissionDO)
                );

                // 组织角色ID-权限 关系
                Map<String, List<String>> roleIdPermissionDOMap = Maps.newHashMap();

                // 循环所有的角色对象
                roleDOS.forEach(roleDO -> {
                    // 当前角色id
                    Long roleDOId = roleDO.getId();
                    // 当前角色roleKey
                    String roleKey = roleDO.getRoleKey();
                    // 获取当前角色id 对应的所有权限id
                    List<Long> permissionIds = roleIdPermissionIdsMap.get(roleDOId);
                    if (CollUtil.isNotEmpty(permissionIds)) {
                        // 如果当前角色有对应的权限id不为空
                        List<String> permissionDOList = Lists.newArrayList();
                        // 遍历所有的权限id
                        permissionIds.forEach(permissionId -> {
                            // 根据权限id 获取到权限对象
                            PermissionDO permissionDO = permissionDOMap.get(permissionId);
                            permissionDOList.add(permissionDO.getPermissionKey());
                        });
                        roleIdPermissionDOMap.put(roleKey, permissionDOList);
                    }
                });

                // 同步至 Redis 中，方便后续网关查询鉴权使用
                roleIdPermissionDOMap.forEach((roleKey, permissionDO) -> {
                    String key = RedisKeyConstants.buildRolePermissionsKey(roleKey);
                    redisTemplate.opsForValue().set(key, JsonUtils.toJsonString(permissionDO));
                });
            }

        } catch (Exception e) {
            log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
        }

    }
}
