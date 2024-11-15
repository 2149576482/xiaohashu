package com.arnanzz.xiaohashu.user.api;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.user.constant.ApiConstants;
import com.arnanzz.xiaohashu.user.dto.req.FindUserByPhoneReqDTO;
import com.arnanzz.xiaohashu.user.dto.req.RegisterUserReqDTO;
import com.arnanzz.xiaohashu.user.dto.req.UpdateUserPasswordReqDTO;
import com.arnanzz.xiaohashu.user.dto.resp.FindUserByPhoneRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 调用用户服务接口
 **/
@FeignClient(name = ApiConstants.SERVER_NAME)
public interface UserFeignApi {

    // 定义前缀
    String PREFIX = "/user";

    /**
     * 用户注册
     */
    @PostMapping(value = PREFIX + "/register")
    Response<Long> registerUser(@RequestBody RegisterUserReqDTO registerUserReqDTO);

    /**
     * 根据手机号 查询用户
     */
    @PostMapping(value = PREFIX + "/findByPhone")
    Response<FindUserByPhoneRspDTO> findByPhone(@RequestBody FindUserByPhoneReqDTO findUserByPhoneReqDTO);

    /**
     * 更新密码
     */
    @PostMapping(value = PREFIX + "/password/update")
    Response<?> updatePassword(@RequestBody UpdateUserPasswordReqDTO updateUserPasswordReqDTO);
}
