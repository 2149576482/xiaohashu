package com.arnanzz.xiaohashu.auth.domain.mapper;

import com.arnanzz.xiaohashu.auth.domain.entity.PermissionDO;

import java.util.List;

public interface PermissionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(PermissionDO record);

    PermissionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PermissionDO record);


    /**
     * 查询 APP 端所有被启用的权限
     */
    List<PermissionDO> selectAppEnabledList();
}