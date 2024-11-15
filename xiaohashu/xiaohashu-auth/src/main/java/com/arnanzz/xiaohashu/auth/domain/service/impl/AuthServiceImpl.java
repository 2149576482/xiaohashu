package com.arnanzz.xiaohashu.auth.domain.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.arnanzz.framework.biz.context.holder.LoginUserContextHolder;
import com.arnanzz.framework.common.exception.BizException;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.auth.constant.RedisKeyConstants;
import com.arnanzz.xiaohashu.auth.enums.LoginTypeEnum;
import com.arnanzz.xiaohashu.auth.enums.ResponseCodeEnum;
import com.arnanzz.xiaohashu.auth.model.vo.user.UpdatePasswordReqVO;
import com.arnanzz.xiaohashu.auth.model.vo.user.UserLoginReqVO;
import com.arnanzz.xiaohashu.auth.rpc.UserRpcService;
import com.arnanzz.xiaohashu.user.dto.resp.FindUserByPhoneRspDTO;
import com.google.common.base.Preconditions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.arnanzz.xiaohashu.auth.domain.service.AuthService;

import java.util.Objects;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserRpcService userRpcService;
    /**
     * 登录与注册
     */
    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {

        String phone = userLoginReqVO.getPhone();
        Integer type = userLoginReqVO.getType();

        // 获取登录类型
        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);
        Long userId = null;

        switch (Objects.requireNonNull(loginTypeEnum)) {
            case VERIFICATION_CODE: // 验证码登录
                String verificationCode = userLoginReqVO.getCode();

                // 校验入参验证码是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(verificationCode), "验证码不能为空");

                String redisKey = RedisKeyConstants.buildVerificationCodeKey(phone);
                // 获取redis缓存中的 验证码
                String codeValue = (String) redisTemplate.opsForValue().get(redisKey);

                // 校验验证码
                if (!StringUtils.equals(verificationCode, codeValue)) {
                    throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }

                // RPC: 调用用户服务，注册用户
                Long userIdTmp = userRpcService.registerUser(phone);

                // 若调用用户服务，返回的用户 ID 为空，则提示登录失败
                if (Objects.isNull(userIdTmp)) {
                    throw new BizException(ResponseCodeEnum.LOGIN_FAIL);
                }

                userId = userIdTmp;
                break;
            case PASSWORD: // 密码登录
                String password = userLoginReqVO.getPassword();
                FindUserByPhoneRspDTO userByPhoneRspDTO = userRpcService.findByPhone(phone);

                // 判断该手机号是否注册
                if (Objects.isNull(userByPhoneRspDTO)) {
                    throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
                }

                // 密文密码
                String encodePassword = userByPhoneRspDTO.getPassword();

                // 进行判断
                boolean result = passwordEncoder.matches(password, encodePassword);

                if (!result) {
                    throw new BizException(ResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
                }
                userId = userByPhoneRspDTO.getId();
                break;
            default:
                break;
        }
        // SaToken 登录 返回Token
        StpUtil.login(userId);
        return Response.success(StpUtil.getTokenValue());
    }

    /**
     * 修改密码
     */
    @Override
    public Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO) {
        // 新密码
        String newPassword = updatePasswordReqVO.getNewPassword();
        // 密码加密
        String encodePassword = passwordEncoder.encode(newPassword);

        // 调用用户服务 更新密码
        userRpcService.updatePassword(encodePassword);

        return Response.success();
    }

    @Override
    public Response<?> logout() {
        // 退出登录
        Long userId = LoginUserContextHolder.getUserId();
        log.info("==> 用户退出登录, userId: {}", userId);
        StpUtil.logout(userId);
        return Response.success();
    }
}
