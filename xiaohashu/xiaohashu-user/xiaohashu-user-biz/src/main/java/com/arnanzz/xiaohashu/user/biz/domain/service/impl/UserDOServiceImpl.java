package com.arnanzz.xiaohashu.user.biz.domain.service.impl;


import com.arnanzz.framework.biz.context.holder.LoginUserContextHolder;
import com.arnanzz.framework.common.enums.DeletedEnum;
import com.arnanzz.framework.common.enums.StatusEnum;
import com.arnanzz.framework.common.exception.BizException;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.framework.common.util.JsonUtils;
import com.arnanzz.framework.common.util.ParamUtils;
import com.arnanzz.xiaohashu.user.biz.constant.RedisKeyConstants;
import com.arnanzz.xiaohashu.user.biz.constant.RoleConstants;
import com.arnanzz.xiaohashu.user.biz.domain.entity.RoleDO;
import com.arnanzz.xiaohashu.user.biz.domain.entity.UserDO;
import com.arnanzz.xiaohashu.user.biz.domain.entity.UserRoleRelDO;
import com.arnanzz.xiaohashu.user.biz.domain.mapper.RoleDOMapper;
import com.arnanzz.xiaohashu.user.biz.domain.mapper.UserDOMapper;
import com.arnanzz.xiaohashu.user.biz.domain.mapper.UserRoleRelDOMapper;
import com.arnanzz.xiaohashu.user.biz.domain.service.UserDOService;
import com.arnanzz.xiaohashu.user.biz.enums.ResponseCodeEnum;
import com.arnanzz.xiaohashu.user.biz.enums.SexEnum;
import com.arnanzz.xiaohashu.user.biz.model.vo.user.UpdateUserInfoReqVO;
import com.arnanzz.xiaohashu.user.biz.rpc.DistributedIdGeneratorRpcService;
import com.arnanzz.xiaohashu.user.biz.rpc.OssRpcService;
import com.arnanzz.xiaohashu.user.dto.req.FindUserByPhoneReqDTO;
import com.arnanzz.xiaohashu.user.dto.req.RegisterUserReqDTO;
import com.arnanzz.xiaohashu.user.dto.req.UpdateUserPasswordReqDTO;
import com.arnanzz.xiaohashu.user.dto.resp.FindUserByPhoneRspDTO;
import com.google.common.base.Preconditions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户模块实现类
 */
@Slf4j
@Service
public class UserDOServiceImpl implements UserDOService {

    @Resource
    private UserDOMapper userDOMapper;

    @Resource
    private OssRpcService ossRpcService;

    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserRoleRelDOMapper userRoleRelDOMapper;

    @Resource
    private RoleDOMapper roleDOMapper;

    /**
     * 更新用户密码
     */
    @Override
    public Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO) {
        String encodePassword = updateUserPasswordReqDTO.getEncodePassword();
        // 获取当前请求用户的ID
        Long userId = LoginUserContextHolder.getUserId();
        UserDO userDO = UserDO.builder()
                .id(userId)
                .password(encodePassword)
                .updateTime(LocalDateTime.now()).build();
        userDOMapper.updateByPrimaryKeySelective(userDO);
        return Response.success();
    }

    /**
     * 根据用户手机号 查询用户信息
     */
    @Override
    public Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO) {
        String phone = findUserByPhoneReqDTO.getPhone();

        // 根据手机号查询用户信息
        UserDO userDO = userDOMapper.selectByPhone(phone);

        // 判空
        if (Objects.isNull(userDO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参
        FindUserByPhoneRspDTO findUserByPhoneRspDTO = FindUserByPhoneRspDTO.builder()
                .id(userDO.getId())
                .password(userDO.getPassword())
                .build();

        return Response.success(findUserByPhoneRspDTO);
    }

    /**
     * 用户注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> register(RegisterUserReqDTO registerUserReqDTO) {
        String phone = registerUserReqDTO.getPhone();

        // 先判断该手机号是否已被注册
        UserDO userDO1 = userDOMapper.selectByPhone(phone);

        log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO1));

        // 若已注册，则直接返回用户 ID
        if (Objects.nonNull(userDO1)) {
            return Response.success(userDO1.getId());
        }

        // 否则注册新用户
        // 获取全局自增的小哈书 ID
        // Long xiaohashuId = redisTemplate.opsForValue().increment(RedisKeyConstants.XIAOHASHU_ID_GENERATOR_KEY);

        // RPC 调用 调用分布式ID 生成 小哈书 ID
        String xiaohashuId = distributedIdGeneratorRpcService.getXiaohashuId();

        // RPC 调用 调用分布式ID 生成 用户 ID
        String userIDStr = distributedIdGeneratorRpcService.getUserId();
        Long userId = Long.valueOf(userIDStr);

        UserDO userDO = UserDO.builder()
                .id(userId)
                .phone(phone)
                .xiaohashuId(xiaohashuId) // 自动生成小红书号 ID
                .nickname("小红薯" + xiaohashuId) // 自动生成昵称, 如：小红薯10000
                .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                .build();

        // 添加入库
        userDOMapper.insertSelective(userDO);

        // 获取刚刚添加入库的用户 ID
        // Long userId = userDO.getId();

        // 给该用户分配一个默认角色
        UserRoleRelDO userRoleDO = UserRoleRelDO.builder()
                .userId(userId)
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        userRoleRelDOMapper.insertSelective(userRoleDO);

        RoleDO roleDO = roleDOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);

        // 将该用户的角色 ID 存入 Redis 中
        List<String> roles = new ArrayList<>(1);
        roles.add(roleDO.getRoleKey());

        String userRolesKey = RedisKeyConstants.buildUserRoleKey(userId);
        redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

        return Response.success(userId);
    }

    /**
     * 更新用户信息
     */
    @Override
    public Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {

        UserDO userDO = new UserDO();
        // 设置当前需要更新的用户 ID
        userDO.setId(LoginUserContextHolder.getUserId());
        // 标识位：是否需要更新
        boolean needUpdate = false;

        // 头像
        MultipartFile avatarFile = updateUserInfoReqVO.getAvatar();

        if (Objects.nonNull(avatarFile)) {
            String avatar = ossRpcService.uploadFile(avatarFile);
            log.info("==> 调用 oss 服务成功，上传头像，url：{}", avatar);

            // 若上传头像失败，则抛出业务异常
            if (StringUtils.isBlank(avatar)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }

            userDO.setAvatar(avatar);
            needUpdate = true;
        }

        // 昵称
        String nickname = updateUserInfoReqVO.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamUtils.checkNickname(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL.getErrorMessage());
            userDO.setNickname(nickname);
            needUpdate = true;
        }

        // 小哈书号
        String xiaohashuId = updateUserInfoReqVO.getXiaohashuId();
        if (StringUtils.isNotBlank(xiaohashuId)) {
            Preconditions.checkArgument(ParamUtils.checkXiaohashuId(xiaohashuId), ResponseCodeEnum.XIAOHASHU_ID_VALID_FAIL.getErrorMessage());
            userDO.setXiaohashuId(xiaohashuId);
            needUpdate = true;
        }

        // 性别
        Integer sex = updateUserInfoReqVO.getSex();
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL.getErrorMessage());
            userDO.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = updateUserInfoReqVO.getBirthday();
        if (Objects.nonNull(birthday)) {
            userDO.setBirthday(birthday);
            needUpdate = true;
        }

        // 个人简介
        String introduction = updateUserInfoReqVO.getIntroduction();
        if (StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL.getErrorMessage());
            userDO.setIntroduction(introduction);
            needUpdate = true;
        }

        // 背景图
        MultipartFile backgroundImgFile = updateUserInfoReqVO.getBackgroundImg();
        if (Objects.nonNull(backgroundImgFile)) {
            String backgroundImg = ossRpcService.uploadFile(backgroundImgFile);
            log.info("==> 调用 oss 服务成功，上传背景图，url：{}", backgroundImg);

            // 若上传背景图失败，则抛出业务异常
            if (StringUtils.isBlank(backgroundImg)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }

            userDO.setBackgroundImg(backgroundImg);
            needUpdate = true;
        }

        if (needUpdate) {
            // 更新用户信息
            userDO.setUpdateTime(LocalDateTime.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);
        }
        return Response.success();
    }
}
