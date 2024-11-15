package com.arnanzz.xiaohashu.auth.domain.service;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.auth.model.vo.verificationcode.SendVerificationCodeReqVO;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
public interface VerificationCodeService {

    /**
     * 发送短信验证码
     */
    Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO);

}
