package com.arnanzz.xiaohashu.auth.domain.service;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.auth.model.vo.user.UserLoginReqVO;

public interface UserDOService{

    /**
     * 登录与注册
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);

}
