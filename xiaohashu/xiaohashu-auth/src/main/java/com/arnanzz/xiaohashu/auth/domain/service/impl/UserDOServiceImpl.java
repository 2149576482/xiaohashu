package com.arnanzz.xiaohashu.auth.domain.service.impl;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import com.arnanzz.xiaohashu.auth.domain.entity.UserDO;
import com.arnanzz.xiaohashu.auth.domain.mapper.UserDOMapper;
import com.arnanzz.xiaohashu.auth.domain.service.UserDOService;
@Service
public class UserDOServiceImpl implements UserDOService{

    @Autowired
    private UserDOMapper userDOMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return userDOMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(UserDO record) {
        return userDOMapper.insert(record);
    }

    @Override
    public int insertSelective(UserDO record) {
        return userDOMapper.insertSelective(record);
    }

    @Override
    public UserDO selectByPrimaryKey(Long id) {
        return userDOMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(UserDO record) {
        return userDOMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(UserDO record) {
        return userDOMapper.updateByPrimaryKey(record);
    }

}
