package com.arnanzz.xiaohashu.auth.rpc;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.user.api.UserFeignApi;
import com.arnanzz.xiaohashu.user.dto.req.FindUserByPhoneReqDTO;
import com.arnanzz.xiaohashu.user.dto.req.RegisterUserReqDTO;
import com.arnanzz.xiaohashu.user.dto.req.UpdateUserPasswordReqDTO;
import com.arnanzz.xiaohashu.user.dto.resp.FindUserByPhoneRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 用户服务调用
 **/
@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 调用用户服务 注册用户
     */
    public Long registerUser(String phone) {
        RegisterUserReqDTO registerUserReqDTO = RegisterUserReqDTO.builder().phone(phone).build();
        // 调用用户服务注册用户
        Response<Long> response = userFeignApi.registerUser(registerUserReqDTO);

        if (!response.isSuccess()) {
            return null;
        }

        // 返回用户id
        return response.getData();
    }

    /**
     * 调用用户服务 通过手机号查询用户
     */
    public FindUserByPhoneRspDTO findByPhone(String phone) {
        FindUserByPhoneReqDTO findUserByPhoneReqDTO = FindUserByPhoneReqDTO.builder().phone(phone).build();

        // 调用
        Response<FindUserByPhoneRspDTO> findUserByPhoneRspDTOResponse = userFeignApi.findByPhone(findUserByPhoneReqDTO);

        if (!findUserByPhoneRspDTOResponse.isSuccess()) {
            return null;
        }

        // 返回
        return findUserByPhoneRspDTOResponse.getData();
    }

    /**
     * 调用用户服务 更新密码
     */
    public boolean updatePassword(String encodePassword) {

        UpdateUserPasswordReqDTO updateUserPasswordReqDTO = UpdateUserPasswordReqDTO.builder()
                .encodePassword(encodePassword).build();

        Response<?> response = userFeignApi.updatePassword(updateUserPasswordReqDTO);

        if (response.isSuccess()){
            return true;
        }
        return false;
    }
}
