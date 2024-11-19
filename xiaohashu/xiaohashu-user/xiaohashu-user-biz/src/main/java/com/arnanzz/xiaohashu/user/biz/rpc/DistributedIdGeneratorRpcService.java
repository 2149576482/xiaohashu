package com.arnanzz.xiaohashu.user.biz.rpc;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.distributed.id.generator.api.DistributedGeneratorFeignApi;
import com.arnanzz.xiaohashu.oss.api.FileFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
     * Leaf 号段模式：小哈书 ID 业务标识
     */
    private static final String BIZ_TAG_XIAOHASHU_ID = "leaf-segment-xiaohashu-id";

    /**
     * Leaf 号段模式：用户 ID 业务标识
     */
    private static final String BIZ_TAG_USER_ID = "leaf-segment-user-id";

    /**
     * 调用分布式 ID 生成服务生成小哈书 ID
     */
    public String getXiaohashuId() {
        return distributedGeneratorFeignApi.getSegmentId(BIZ_TAG_XIAOHASHU_ID);
    }

    /**
     * 调用分布式 ID 生成服务用户 ID
     */
    public String getUserId() {
        return distributedGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_ID);
    }
}
