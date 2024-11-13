package com.arnanzz.xiaohashu.oss.biz.factory;

import com.arnanzz.xiaohashu.oss.biz.enums.ResponseCodeEnum;
import com.arnanzz.xiaohashu.oss.biz.strategy.FileStrategy;
import com.arnanzz.xiaohashu.oss.biz.strategy.impl.MinioFileStrategy;
import com.arnanzz.xiaohashu.oss.biz.strategy.impl.AliyunFileStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 工厂类
 **/
@Configuration
@RefreshScope
public class FileStrategyFactory {

    @Value("${storage.type}")
    private String strategyType;

    @Bean
    @RefreshScope
    public FileStrategy getFileStrategy() {
        if (StringUtils.equals(strategyType, "minio")) {
            return new MinioFileStrategy();
        } else if (StringUtils.equals(strategyType, "aliyun")) {
            return new AliyunFileStrategy();
        }

        throw new IllegalArgumentException(ResponseCodeEnum.STORAGE_TYPE_ERROR.getErrorMessage());
    }

}
