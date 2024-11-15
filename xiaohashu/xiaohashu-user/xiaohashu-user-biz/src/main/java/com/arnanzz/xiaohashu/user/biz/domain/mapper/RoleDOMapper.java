package com.arnanzz.xiaohashu.user.biz.domain.mapper;


import com.arnanzz.xiaohashu.user.biz.domain.entity.RoleDO;

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