package com.arnanzz.xiaohashu.user.biz.domain.service;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.user.biz.model.vo.user.UpdateUserInfoReqVO;
import com.arnanzz.xiaohashu.user.dto.req.FindUserByIdReqDTO;
import com.arnanzz.xiaohashu.user.dto.req.FindUserByPhoneReqDTO;
import com.arnanzz.xiaohashu.user.dto.req.RegisterUserReqDTO;
import com.arnanzz.xiaohashu.user.dto.req.UpdateUserPasswordReqDTO;
import com.arnanzz.xiaohashu.user.dto.resp.FindUserByIdRspDTO;
import com.arnanzz.xiaohashu.user.dto.resp.FindUserByPhoneRspDTO;

public interface UserDOService{

    /**
     * 更新用户信息
     */
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);

    /**
     * 用户注册
     */
    Response<Long> register(RegisterUserReqDTO registerUserReqDTO);

    /**
     * 根据手机号 查找用户信息
     */
    Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO);

    /**
     * 更新用户密码
     */
    Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

    /**
     * 根据用户id 查询用户信息
     */
    Response<FindUserByIdRspDTO> findById(FindUserByIdReqDTO findUserByIdReqDTO);
}
