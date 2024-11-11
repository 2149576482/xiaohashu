package com.arnanzz.xiaohashu.auth.domain.service.impl;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import com.arnanzz.xiaohashu.auth.domain.entity.RoleDO;
import com.arnanzz.xiaohashu.auth.domain.mapper.RoleDOMapper;
import com.arnanzz.xiaohashu.auth.domain.service.RoleDOService;
@Service
public class RoleDOServiceImpl implements RoleDOService{

    @Autowired
    private RoleDOMapper roleDOMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return roleDOMapper.deleteByPrimaryKey(id);
    }


    @Override
    public int insertSelective(RoleDO record) {
        return roleDOMapper.insertSelective(record);
    }

    @Override
    public RoleDO selectByPrimaryKey(Long id) {
        return roleDOMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(RoleDO record) {
        return roleDOMapper.updateByPrimaryKeySelective(record);
    }

}
