package com.arnanzz.xiaohashu.auth.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 短信服务 接收配置文件的信息
 **/
@ConfigurationProperties(prefix = "aliyun")
@Component
@Data
public class AliyunAccessKeyProperties {

    private String accessKeyId;

    private String accessKeySecret;

}
