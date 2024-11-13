package com.arnanzz.xiaohashu.oss.biz.strategy.impl;

import com.aliyun.oss.OSS;
import com.arnanzz.framework.common.exception.BizException;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.oss.biz.config.AliyunProperties;
import com.arnanzz.xiaohashu.oss.biz.enums.ResponseCodeEnum;
import com.arnanzz.xiaohashu.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 阿里oss 策略类
 **/
@Slf4j
public class AliyunFileStrategy implements FileStrategy {

    @Resource
    private AliyunProperties aliyunProperties;

    @Resource
    private OSS ossClient;

    @Override
    @SneakyThrows
    public String uploadFile(MultipartFile file, String bucketName) {
        log.info("## 上传文件至阿里云 OSS ...");

        // 判断文件是否为空
        if (file == null || file.getSize() == 0) {
            log.error("==> 上传文件异常：文件大小为空 ...");
            throw new BizException(ResponseCodeEnum.FILE_NOT_EXIST);
        }

        // 文件的原始名称
        String originalFileName = file.getOriginalFilename();

        // 生成存储对象的名称（将 UUID 字符串中的 - 替换成空字符串）
        String key = UUID.randomUUID().toString().replace("-", "");
        // 获取文件的后缀，如 .jpg
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));

        // 拼接上文件后缀，即为要存储的文件名
        String objectName = String.format("%s%s", key, suffix);

        log.info("==> 开始上传文件至阿里云 OSS, ObjectName: {}", objectName);

        // 上传文件至阿里云 OSS
        ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(file.getInputStream().readAllBytes()));

        // 返回文件的访问链接
        String url = String.format("https://%s.%s/%s", bucketName, aliyunProperties.getEndpoint(), objectName);
        log.info("==> 上传文件至阿里云 OSS 成功，访问路径: {}", url);
        return url;
    }
}