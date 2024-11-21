package com.arnanzz.xiaohashu.note.biz.domain.service.impl;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import com.arnanzz.xiaohashu.note.biz.domain.mapper.ChannelDOMapper;
import com.arnanzz.xiaohashu.note.biz.domain.entity.ChannelDO;
import com.arnanzz.xiaohashu.note.biz.domain.service.ChannelDOService;
@Service
public class ChannelDOServiceImpl implements ChannelDOService{

    @Autowired
    private ChannelDOMapper channelDOMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return channelDOMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ChannelDO record) {
        return channelDOMapper.insert(record);
    }

    @Override
    public int insertSelective(ChannelDO record) {
        return channelDOMapper.insertSelective(record);
    }

    @Override
    public ChannelDO selectByPrimaryKey(Long id) {
        return channelDOMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ChannelDO record) {
        return channelDOMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ChannelDO record) {
        return channelDOMapper.updateByPrimaryKey(record);
    }

}
