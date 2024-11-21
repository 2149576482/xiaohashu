package com.arnanzz.xiaohashu.note.biz.domain.service.impl;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import com.arnanzz.xiaohashu.note.biz.domain.mapper.ChannelTopicRelDOMapper;
import com.arnanzz.xiaohashu.note.biz.domain.entity.ChannelTopicRelDO;
import com.arnanzz.xiaohashu.note.biz.domain.service.ChannelTopicRelDOService;
@Service
public class ChannelTopicRelDOServiceImpl implements ChannelTopicRelDOService{

    @Autowired
    private ChannelTopicRelDOMapper channelTopicRelDOMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return channelTopicRelDOMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ChannelTopicRelDO record) {
        return channelTopicRelDOMapper.insert(record);
    }

    @Override
    public int insertSelective(ChannelTopicRelDO record) {
        return channelTopicRelDOMapper.insertSelective(record);
    }

    @Override
    public ChannelTopicRelDO selectByPrimaryKey(Long id) {
        return channelTopicRelDOMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ChannelTopicRelDO record) {
        return channelTopicRelDOMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ChannelTopicRelDO record) {
        return channelTopicRelDOMapper.updateByPrimaryKey(record);
    }

}
