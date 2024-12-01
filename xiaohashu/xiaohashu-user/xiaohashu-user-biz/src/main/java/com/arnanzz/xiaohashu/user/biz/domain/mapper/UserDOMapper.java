package com.arnanzz.xiaohashu.user.biz.domain.mapper;

import com.arnanzz.xiaohashu.user.biz.domain.entity.UserDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDOMapper {

    /**
     * 根据id 删除用户
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 按需新增用户
     */
    int insertSelective(UserDO record);

    /**
     * 根据id查询用户
     */
    UserDO selectByPrimaryKey(Long id);

    /**
     * 按需更新用户
     */
    int updateByPrimaryKeySelective(UserDO record);

    /**
     * 根据手机号查询用户
     */
    UserDO selectByPhone(String phone);

    /**
     * 批量查询用户信息
     */
    List<UserDO> selectByIds(@Param("ids") List<Long> ids);
}