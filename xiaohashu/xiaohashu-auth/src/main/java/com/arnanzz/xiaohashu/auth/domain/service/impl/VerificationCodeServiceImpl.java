package com.arnanzz.xiaohashu.auth.domain.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.arnanzz.framework.common.exception.BizException;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.auth.constant.RedisKeyConstants;
import com.arnanzz.xiaohashu.auth.domain.service.VerificationCodeService;
import com.arnanzz.xiaohashu.auth.enums.ResponseCodeEnum;
import com.arnanzz.xiaohashu.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.arnanzz.xiaohashu.auth.sms.AliyunSmsHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private AliyunSmsHelper aliyunSmsHelper;

    /**
     * 发送短信
     */
    @Override
    public Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        String phone = sendVerificationCodeReqVO.getPhone();
        // 构建key
        String key = RedisKeyConstants.buildVerificationCodeKey(phone);
        // 判断是否存在该key
        Boolean result = redisTemplate.hasKey(key);
        if (result) {
            // 如果key没有过期 抛出异常
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }
        // 生成6位随机数
        String verificationCode  = RandomUtil.randomNumbers(6);

        // 调用第三方短信发送服务
        threadPoolTaskExecutor.submit(() -> {
            String signName = "阿里云短信测试";
            String templateCode = "SMS_154950909";
            String templateParam = String.format("{\"code\":\"%s\"}", verificationCode);
            aliyunSmsHelper.sendMessage(signName, templateCode, phone, templateParam);
        });

        log.info("==> 手机号: {}, 已发送验证码：【{}】", phone, verificationCode);
        redisTemplate.opsForValue().set(key, verificationCode, 3, TimeUnit.MINUTES);
        return Response.success();
    }


}
