package com.arnanzz.xiaohashu.auth.domain;

import com.alibaba.druid.filter.config.ConfigTools;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
@Slf4j
public class PasswordEncoding {

    public static void main(String[] args) throws Exception {
            // 你的密码
            String password = "root";
            String[] arr = ConfigTools.genKeyPair(512);

            // 私钥
            log.info("privateKey: {}", arr[0]);
            // 公钥
            log.info("publicKey: {}", arr[1]);

            // 通过私钥加密密码
            String encodePassword = ConfigTools.encrypt(arr[0], password);
            log.info("password: {}", encodePassword);
    }

}
