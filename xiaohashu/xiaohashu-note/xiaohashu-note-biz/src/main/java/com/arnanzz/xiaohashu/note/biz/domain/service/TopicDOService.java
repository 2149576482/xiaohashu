package com.arnanzz.xiaohashu.note.biz.domain.service;

import com.arnanzz.xiaohashu.note.biz.domain.entity.TopicDO;
public interface TopicDOService{

    int deleteByPrimaryKey(Long id);

    int insert(TopicDO record);

    int insertSelective(TopicDO record);

    TopicDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TopicDO record);

    int updateByPrimaryKey(TopicDO record);

}
