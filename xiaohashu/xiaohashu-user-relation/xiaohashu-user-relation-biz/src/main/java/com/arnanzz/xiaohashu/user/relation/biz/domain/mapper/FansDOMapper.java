package com.arnanzz.xiaohashu.user.relation.biz.domain.mapper;

import com.arnanzz.xiaohashu.user.relation.biz.domain.entity.FansDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
public interface FansDOMapper {

    int deleteByPrimaryKey(Long id);
    int insert(FansDO record);
    int insertSelective(FansDO record);
    FansDO selectByPrimaryKey(Long id);
    int updateByPrimaryKeySelective(FansDO record);
    int updateByPrimaryKey(FansDO record);

    int deleteByUserIdAndFansUserId(@Param("userId") Long userId,
                                    @Param("fansUserId") Long fansUserId);

    /**
     * 查询记录总数
     */
    long selectCountByUserId(Long userId);

    /**
     * 查询最新关注的 5000 位粉丝
     */
    List<FansDO> select5000FansByUserId(Long userId);

    /**
     * 分页查询
     */
    List<FansDO> selectPageListByUserId(@Param("userId") Long userId,
                                        @Param("offset") long offset,
                                        @Param("limit") long limit);
}
