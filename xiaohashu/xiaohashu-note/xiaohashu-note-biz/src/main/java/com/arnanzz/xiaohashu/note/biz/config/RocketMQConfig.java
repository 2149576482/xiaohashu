package com.arnanzz.xiaohashu.note.biz.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 由于 rocketmq-spring-boot-starter 本身是基于 Spring Boot 2.x 开发的，2.x和 3.x 在定义 starter 时的格式是有区别的，
 * 存在兼容性问题。为了能够在 Spring Boot 3.x 中使用它，需要在 /config 包下，创建一个 RocketMQConfig 配置类，
 * 并添加 @Import(RocketMQAutoConfiguration.class) , 手动引入一下自动配置类
 **/
@Configuration
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
}
