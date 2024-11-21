package com.arnanzz.xiaohashu.note.biz.domain.service.impl;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import com.arnanzz.xiaohashu.note.biz.domain.mapper.TopicDOMapper;
import com.arnanzz.xiaohashu.note.biz.domain.entity.TopicDO;
import com.arnanzz.xiaohashu.note.biz.domain.service.TopicDOService;
@Service
public class TopicDOServiceImpl implements TopicDOService{

    @Autowired
    private TopicDOMapper topicDOMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return topicDOMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(TopicDO record) {
        return topicDOMapper.insert(record);
    }

    @Override
    public int insertSelective(TopicDO record) {
        return topicDOMapper.insertSelective(record);
    }

    @Override
    public TopicDO selectByPrimaryKey(Long id) {
        return topicDOMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(TopicDO record) {
        return topicDOMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(TopicDO record) {
        return topicDOMapper.updateByPrimaryKey(record);
    }

}
