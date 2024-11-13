package com.arnanzz.xiaohashu.oss.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 加载配置文件中 minio的配置
 **/
@Data
@Component
@ConfigurationProperties(prefix = "storage.aliyun-oss")
public class AliyunProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;

}
