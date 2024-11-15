package com.arnanzz.xiaohashu.auth.domain.service;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.auth.model.vo.user.UpdatePasswordReqVO;
import com.arnanzz.xiaohashu.auth.model.vo.user.UserLoginReqVO;

public interface AuthService {

    /**
     * 登录与注册
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);

    /**
     * 退出登录
     */
    Response<?> logout();

    /**
     * 修改密码
     */
    Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO);
}
