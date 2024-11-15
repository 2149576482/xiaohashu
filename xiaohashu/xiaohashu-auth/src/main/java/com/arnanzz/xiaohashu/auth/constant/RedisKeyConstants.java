package com.arnanzz.xiaohashu.auth.constant;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 统一 管理 redis Key
 **/
public class RedisKeyConstants {

    /**
     * 验证码 KEY 前缀
     */
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification_code:";

    /**
     * 构建验证码 KEY
     */
    public static String buildVerificationCodeKey(String phone) {
        return VERIFICATION_CODE_KEY_PREFIX + phone;
    }

}
