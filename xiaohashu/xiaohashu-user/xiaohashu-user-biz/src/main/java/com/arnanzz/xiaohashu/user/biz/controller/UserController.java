package com.arnanzz.xiaohashu.user.biz.controller;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.user.biz.domain.service.UserDOService;
import com.arnanzz.xiaohashu.user.biz.model.vo.user.UpdateUserInfoReqVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 用户信息修改
     */
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> updateUserInfo(@Validated UpdateUserInfoReqVO updateUserInfoReqVO) {
        return userDOService.updateUserInfo(updateUserInfoReqVO);
    }

}
