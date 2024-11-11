package com.arnanzz.xiaohashu.auth.domain.service;

import com.arnanzz.xiaohashu.auth.domain.entity.PermissionDO;
public interface PermissionDOService{

    int deleteByPrimaryKey(Long id);

    int insertSelective(PermissionDO record);

    PermissionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PermissionDO record);

}
