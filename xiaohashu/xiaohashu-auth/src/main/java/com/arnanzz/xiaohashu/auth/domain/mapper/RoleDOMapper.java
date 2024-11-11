package com.arnanzz.xiaohashu.auth.domain.mapper;

import com.arnanzz.xiaohashu.auth.domain.entity.RoleDO;

import javax.management.relation.Role;
import java.util.List;

public interface RoleDOMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(RoleDO record);

    RoleDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RoleDO record);

    /**
     * 查询所有被启用的角色
     */
    List<RoleDO> selectEnabledList();
}