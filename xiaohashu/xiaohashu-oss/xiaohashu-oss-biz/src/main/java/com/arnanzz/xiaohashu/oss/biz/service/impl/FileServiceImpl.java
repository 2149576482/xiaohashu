package com.arnanzz.xiaohashu.oss.biz.service.impl;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.oss.biz.service.FileService;
import com.arnanzz.xiaohashu.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
@Service
public class FileServiceImpl implements FileService {

    @Resource
    private FileStrategy fileStrategy;

    private static final String BUCKET_NAME = "xiaohashu";

    /**
     * 上传
     */
    @Override
    public Response<?> uploadFile(MultipartFile file) {
        String url = fileStrategy.uploadFile(file, BUCKET_NAME);
        return Response.success(url);
    }
}
