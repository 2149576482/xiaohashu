package com.arnanzz.xiaohashu.auth.controller;

import com.arnanzz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.arnanzz.xiaohashu.auth.domain.service.VerificationCodeService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 发送验证码接口
 **/
@RestController
@RequestMapping("/verification")
public class VerificationCodeController {

    @Resource
    private VerificationCodeService verificationCodeService;

    /**
     * 发送验证码
     */
    @PostMapping("/code/send")
    @ApiOperationLog(description = "发送验证码")
    public Response<?> send(@Validated @RequestBody SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        return verificationCodeService.send(sendVerificationCodeReqVO);
    }

}
