package com.arnanzz.xiaohashu.note.biz.domain.service;

import com.arnanzz.xiaohashu.note.biz.domain.entity.ChannelDO;
public interface ChannelDOService{

    int deleteByPrimaryKey(Long id);

    int insert(ChannelDO record);

    int insertSelective(ChannelDO record);

    ChannelDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ChannelDO record);

    int updateByPrimaryKey(ChannelDO record);

}
