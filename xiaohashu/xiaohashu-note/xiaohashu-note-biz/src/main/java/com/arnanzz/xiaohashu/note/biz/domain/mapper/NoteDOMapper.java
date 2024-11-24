package com.arnanzz.xiaohashu.note.biz.domain.mapper;

import com.arnanzz.xiaohashu.note.biz.domain.entity.NoteDO;

public interface NoteDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteDO record);

    int insertSelective(NoteDO record);

    NoteDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteDO record);

    int updateByPrimaryKey(NoteDO record);

    /**
     * 修改可见性
     */
    int updateVisibleOnlyMe(NoteDO noteDO);

    /**
     * 修改置顶状态
     */
    int updateIsTop(NoteDO noteDO);
}