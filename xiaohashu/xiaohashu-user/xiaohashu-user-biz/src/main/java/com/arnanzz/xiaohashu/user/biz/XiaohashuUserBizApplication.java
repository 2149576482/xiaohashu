package com.arnanzz.xiaohashu.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
@SpringBootApplication
@MapperScan("com.arnanzz.xiaohashu.user.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.arnanzz.xiaohashu")
public class XiaohashuUserBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaohashuUserBizApplication.class, args);
    }

}
