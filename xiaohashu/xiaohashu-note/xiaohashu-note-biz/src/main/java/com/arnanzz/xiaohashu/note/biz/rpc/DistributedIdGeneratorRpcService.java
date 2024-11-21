package com.arnanzz.xiaohashu.note.biz.rpc;

import com.arnanzz.xiaohashu.distributed.id.generator.api.DistributedGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: ID 自动生成服务调用
 **/
@Component
public class DistributedIdGeneratorRpcService {

    @Resource
    private DistributedGeneratorFeignApi distributedGeneratorFeignApi;

    /**
     * 生成雪花算法id
     */
    public String getSnowflakeId() {
        return distributedGeneratorFeignApi.getSnowflakeId("test");
    }
}
