package com.arnanzz.xiaohashu.note.biz.domain.mapper;

import com.arnanzz.xiaohashu.note.biz.domain.entity.TopicDO;

public interface TopicDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TopicDO record);

    int insertSelective(TopicDO record);

    TopicDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TopicDO record);

    int updateByPrimaryKey(TopicDO record);

    /**
     * 根据id 查询话题名称
     */
    String selectNameByPrimaryKey(Long id);
}