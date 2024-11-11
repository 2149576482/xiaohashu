package com.arnanzz.xiaohashu.auth.domain.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.arnanzz.framework.common.enums.DeletedEnum;
import com.arnanzz.framework.common.enums.StatusEnum;
import com.arnanzz.framework.common.exception.BizException;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.framework.common.util.JsonUtils;
import com.arnanzz.xiaohashu.auth.constant.RedisKeyConstants;
import com.arnanzz.xiaohashu.auth.constant.RoleConstants;
import com.arnanzz.xiaohashu.auth.domain.entity.RoleDO;
import com.arnanzz.xiaohashu.auth.domain.entity.UserRoleRelDO;
import com.arnanzz.xiaohashu.auth.domain.mapper.UserRoleRelDOMapper;
import com.arnanzz.xiaohashu.auth.enums.LoginTypeEnum;
import com.arnanzz.xiaohashu.auth.enums.ResponseCodeEnum;
import com.arnanzz.xiaohashu.auth.model.vo.user.UserLoginReqVO;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.arnanzz.xiaohashu.auth.domain.entity.UserDO;
import com.arnanzz.xiaohashu.auth.domain.mapper.UserDOMapper;
import com.arnanzz.xiaohashu.auth.domain.service.UserDOService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
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
    private TransactionTemplate transactionTemplate;
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
            case PASSWORD:
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
                        .xiaohashuId("小红薯" + xiaohashuId)
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

                // 将该用户的角色id 存入redis
                List<Long> roles = Lists.newArrayList();
                roles.add(RoleConstants.COMMON_USER_ROLE_ID);
                String redisKey = RedisKeyConstants.buildUserRoleKey(phone);
                redisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(roles));
                return userId;
            } catch (Exception e) {
                status.setRollbackOnly(); // 标记事务为回滚
                log.error("==> 系统注册用户异常: ", e);
                return null;
            }
        });
    }
}
