package com.arnanzz.xiaohashu.oss.api;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.oss.config.FeignFormConfig;
import com.arnanzz.xiaohashu.oss.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 调用文件服务接口
 **/
@FeignClient(name = ApiConstants.SERVER_NAME, configuration = FeignFormConfig.class)
public interface FileFeignApi {

    // 定义前缀
    String PREFIX = "/file";

    /**
     * 文件上传
     */
    @PostMapping(value = PREFIX + "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<?> uploadFile(@RequestPart(value = "file") MultipartFile file);

}
