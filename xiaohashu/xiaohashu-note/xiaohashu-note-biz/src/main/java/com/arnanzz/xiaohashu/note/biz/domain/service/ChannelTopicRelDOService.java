package com.arnanzz.xiaohashu.note.biz.domain.service;

import com.arnanzz.xiaohashu.note.biz.domain.entity.ChannelTopicRelDO;
public interface ChannelTopicRelDOService{

    int deleteByPrimaryKey(Long id);

    int insert(ChannelTopicRelDO record);

    int insertSelective(ChannelTopicRelDO record);

    ChannelTopicRelDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ChannelTopicRelDO record);

    int updateByPrimaryKey(ChannelTopicRelDO record);

}
