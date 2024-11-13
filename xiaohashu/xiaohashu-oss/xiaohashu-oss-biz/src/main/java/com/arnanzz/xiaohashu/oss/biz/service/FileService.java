package com.arnanzz.xiaohashu.oss.biz.service;

import com.arnanzz.framework.common.response.Response;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传service服务层
 */
public interface FileService {

    /**
     * 上传文件
     */
    Response<?> uploadFile(MultipartFile file);
}
