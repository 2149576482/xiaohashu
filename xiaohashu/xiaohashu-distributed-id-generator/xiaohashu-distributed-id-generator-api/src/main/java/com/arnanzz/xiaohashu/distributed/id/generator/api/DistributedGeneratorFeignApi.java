package com.arnanzz.xiaohashu.distributed.id.generator.api;

import com.arnanzz.xiaohashu.distributed.id.generator.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 调用自动生成id服务接口
 **/
@FeignClient(name = ApiConstants.SERVER_NAME)
public interface DistributedGeneratorFeignApi {

    // 定义前缀
    String PREFIX = "/id";

    /**
     * 号段模式 自增长
     */
    @GetMapping(value = PREFIX + "/segment/get/{key}")
    String getSegmentId(@PathVariable("key") String key);

    /**
     * 雪花模式
     */
    @GetMapping(value = PREFIX + "/snowflake/get/{key}")
    String getSnowflakeId(@PathVariable("key") String key);
}
