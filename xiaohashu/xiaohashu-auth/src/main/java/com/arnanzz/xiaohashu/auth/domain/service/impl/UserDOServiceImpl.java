package com.arnanzz.xiaohashu.auth.domain.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.arnanzz.framework.biz.context.holder.LoginUserContextHolder;
import com.arnanzz.framework.common.enums.DeletedEnum;
import com.arnanzz.framework.common.enums.StatusEnum;
import com.arnanzz.framework.common.exception.BizException;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.framework.common.util.JsonUtils;
import com.arnanzz.xiaohashu.auth.config.PasswordEncoderConfig;
import com.arnanzz.xiaohashu.auth.constant.RedisKeyConstants;
import com.arnanzz.xiaohashu.auth.constant.RoleConstants;
import com.arnanzz.xiaohashu.auth.domain.entity.RoleDO;
import com.arnanzz.xiaohashu.auth.domain.entity.UserRoleRelDO;
import com.arnanzz.xiaohashu.auth.domain.mapper.RoleDOMapper;
import com.arnanzz.xiaohashu.auth.domain.mapper.UserRoleRelDOMapper;
import com.arnanzz.xiaohashu.auth.enums.LoginTypeEnum;
import com.arnanzz.xiaohashu.auth.enums.ResponseCodeEnum;
import com.arnanzz.xiaohashu.auth.model.vo.user.UpdatePasswordReqVO;
import com.arnanzz.xiaohashu.auth.model.vo.user.UserLoginReqVO;
import com.google.common.base.Preconditions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.arnanzz.xiaohashu.auth.domain.entity.UserDO;
import com.arnanzz.xiaohashu.auth.domain.mapper.UserDOMapper;
import com.arnanzz.xiaohashu.auth.domain.service.UserDOService;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserDOServiceImpl implements UserDOService{

    @Resource
    private UserDOMapper userDOMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserRoleRelDOMapper userRoleRelDOMapper;

    @Resource
    private RoleDOMapper roleDOMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private PasswordEncoder passwordEncoder;
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

                // 通过手机号查询记录
                UserDO userDO = userDOMapper.selectByPhone(phone);

                log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO));

                // 判断是否注册
                if (Objects.isNull(userDO)) {
                    // 若没有注册 则进行注册
                    userId = registerUser(phone);
                } else {
                    // 已经注册 获取信息
                    userId = userDO.getId();
                }
                break;
            case PASSWORD: // 密码登录
                String password = userLoginReqVO.getPassword();
                UserDO userDO1 = userDOMapper.selectByPhone(phone);

                // 判断该手机号是否注册
                if (Objects.isNull(userDO1)) {
                    throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
                }

                // 密文密码
                String encodePassword = userDO1.getPassword();

                // 进行判断
                boolean result = passwordEncoder.matches(password, encodePassword);

                if (!result) {
                    throw new BizException(ResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
                }
                userId = userDO1.getId();
                break;
            default:
                break;
        }
        // SaToken 登录 返回Token
        StpUtil.login(userId);
        return Response.success(StpUtil.getTokenValue());
    }

    /**
     * 用户注册
     */
    public Long registerUser(String phone) {
        return transactionTemplate.execute(status -> {

            try {
                // 获取全局自增的小哈书id
                Long xiaohashuId = redisTemplate.opsForValue().increment(RedisKeyConstants.XIAOHASHU_ID_GENERATOR_KEY);
                UserDO userDO = UserDO.builder()
                        .phone(phone)
                        .xiaohashuId(String.valueOf(xiaohashuId))
                        .nickname("小红薯" + xiaohashuId)
                        .status(StatusEnum.ENABLE.getValue())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue())
                        .build();
                userDOMapper.insertSelective(userDO);
                Long userId = userDO.getId();

                // 给用户分配一个默认的角色
                UserRoleRelDO userRoleRelDO = UserRoleRelDO.builder()
                        .userId(userId)
                        .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue())
                        .build();
                userRoleRelDOMapper.insertSelective(userRoleRelDO);

                // 将该用户的角色 ID 存入 Redis 中，指定初始容量为 1，这样可以减少在扩容时的性能开销
                List<String> roles = new ArrayList<>(1);
                RoleDO roleDO = roleDOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);
                roles.add(roleDO.getRoleKey());
                String redisKey = RedisKeyConstants.buildUserRoleKey(userId);
                redisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(roles));
                return userId;
            } catch (Exception e) {
                status.setRollbackOnly(); // 标记事务为回滚
                log.error("==> 系统注册用户异常: ", e);
                return null;
            }
        });
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

        // 获取当前请求对应的用户 ID
        Long userId = LoginUserContextHolder.getUserId();

        UserDO userDO = UserDO.builder()
                .id(userId)
                .password(encodePassword)
                .updateTime(LocalDateTime.now())
                .build();
        // 更新密码
        userDOMapper.updateByPrimaryKeySelective(userDO);

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
