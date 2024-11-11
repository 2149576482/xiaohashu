package com.arnanzz.xiaohashu.auth.domain.service.impl;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import com.arnanzz.xiaohashu.auth.domain.entity.PermissionDO;
import com.arnanzz.xiaohashu.auth.domain.mapper.PermissionDOMapper;
import com.arnanzz.xiaohashu.auth.domain.service.PermissionDOService;
@Service
public class PermissionDOServiceImpl implements PermissionDOService{

    @Autowired
    private PermissionDOMapper permissionDOMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return permissionDOMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insertSelective(PermissionDO record) {
        return permissionDOMapper.insertSelective(record);
    }

    @Override
    public PermissionDO selectByPrimaryKey(Long id) {
        return permissionDOMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(PermissionDO record) {
        return permissionDOMapper.updateByPrimaryKeySelective(record);
    }

}
