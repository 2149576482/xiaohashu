package com.arnanzz.xiaohashu.auth.domain.mapper;

import com.arnanzz.xiaohashu.auth.domain.entity.RolePermissionRelDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RolePermissionRelDOMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(RolePermissionRelDO record);

    RolePermissionRelDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePermissionRelDO record);

    /**
     * 根据角色 ID 集合批量查询
     */
    List<RolePermissionRelDO> selectByRoleIds(@Param("roleIds") List<Long> roleIds);
}