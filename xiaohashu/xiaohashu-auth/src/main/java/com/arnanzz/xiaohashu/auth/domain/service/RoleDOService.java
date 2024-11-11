package com.arnanzz.xiaohashu.auth.domain.service;

import com.arnanzz.xiaohashu.auth.domain.entity.RoleDO;
public interface RoleDOService{

    int deleteByPrimaryKey(Long id);

    int insertSelective(RoleDO record);

    RoleDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RoleDO record);

}
