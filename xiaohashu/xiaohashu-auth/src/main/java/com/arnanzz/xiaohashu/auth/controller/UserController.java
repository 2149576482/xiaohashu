package com.arnanzz.xiaohashu.auth.controller;

import com.arnanzz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.auth.domain.service.UserDOService;
import com.arnanzz.xiaohashu.auth.model.vo.user.UpdatePasswordReqVO;
import com.arnanzz.xiaohashu.auth.model.vo.user.UserLoginReqVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 用户模块接口
 **/
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserDOService userDOService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ApiOperationLog(description = "用户登录/注册")
    public Response<String> loginAndRegister(@Validated @RequestBody UserLoginReqVO userLoginReqVO) {
        return userDOService.loginAndRegister(userLoginReqVO);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password/update")
    @ApiOperationLog(description = "修改密码")
    public Response<?> updatePassword(@Validated @RequestBody UpdatePasswordReqVO updatePasswordReqVO) {
        return userDOService.updatePassword(updatePasswordReqVO);
    }

    @PostMapping("/logout")
    @ApiOperationLog(description = "账号登出")
    public Response<?> logout() {
        return userDOService.logout();
    }
}
