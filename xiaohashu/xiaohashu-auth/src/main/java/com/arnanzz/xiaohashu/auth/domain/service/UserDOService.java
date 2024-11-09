package com.arnanzz.xiaohashu.auth.domain.service;

import com.arnanzz.xiaohashu.auth.domain.entity.UserDO;
public interface UserDOService{

    int deleteByPrimaryKey(Long id);

    int insert(UserDO record);

    int insertSelective(UserDO record);

    UserDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDO record);

    int updateByPrimaryKey(UserDO record);

}
