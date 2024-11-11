package com.arnanzz.xiaohashu.auth.domain.service;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.auth.model.vo.verificationcode.SendVerificationCodeReqVO;

public interface VerificationCodeService {

    /**
     * 发送短信验证码
     *
     * @param sendVerificationCodeReqVO
     * @return
     */
    Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO);

}
