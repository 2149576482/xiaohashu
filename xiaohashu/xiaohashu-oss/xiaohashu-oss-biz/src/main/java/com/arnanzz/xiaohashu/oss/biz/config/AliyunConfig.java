package com.arnanzz.xiaohashu.oss.biz.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 加载aliyun客户端
 **/
@Configuration
public class AliyunConfig {

    @Resource
    private AliyunProperties aliyunProperties;

    /**
     * 构建 阿里云 OSS 客户端
     */
    @Bean
    public OSS aliyunOSSClient() {
        // 设置访问凭证
        DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(
                aliyunProperties.getAccessKey(), aliyunProperties.getSecretKey());

        // 创建 OSSClient 实例
        return new OSSClientBuilder().build(aliyunProperties.getEndpoint(), credentialsProvider);
    }

}
