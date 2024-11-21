package com.arnanzz.xiaohashu.note.biz;

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
@MapperScan("com.arnanzz.xiaohashu.note.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.arnanzz.xiaohashu")
public class XiaohashuNoteBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaohashuNoteBizApplication.class, args);
    }

}
