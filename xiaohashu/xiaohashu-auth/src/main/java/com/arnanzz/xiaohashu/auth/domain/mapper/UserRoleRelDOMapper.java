package com.arnanzz.xiaohashu.auth.domain.mapper;

import com.arnanzz.xiaohashu.auth.domain.entity.UserRoleRelDO;

public interface UserRoleRelDOMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(UserRoleRelDO record);

    UserRoleRelDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserRoleRelDO record);
}