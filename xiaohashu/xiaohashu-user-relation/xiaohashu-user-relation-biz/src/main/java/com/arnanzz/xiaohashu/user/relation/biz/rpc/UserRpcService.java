package com.arnanzz.xiaohashu.user.relation.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.user.api.UserFeignApi;
import com.arnanzz.xiaohashu.user.dto.req.FindUserByIdReqDTO;
import com.arnanzz.xiaohashu.user.dto.req.FindUsersByIdsReqDTO;
import com.arnanzz.xiaohashu.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: ID 自动生成服务调用
 **/
@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 查询用户信息
     */
    public FindUserByIdRspDTO findById(Long userId) {
        FindUserByIdReqDTO findUserByIdReqDTO = new FindUserByIdReqDTO();
        findUserByIdReqDTO.setId(userId);

        Response<FindUserByIdRspDTO> response = userFeignApi.findById(findUserByIdReqDTO);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }

        return response.getData();
    }

    /**
     * 批量查询用户信息
     */
    public List<FindUserByIdRspDTO> findByIds(List<Long> userIds) {
        FindUsersByIdsReqDTO findUsersByIdsReqDTO = new FindUsersByIdsReqDTO();
        findUsersByIdsReqDTO.setIds(userIds);

        Response<List<FindUserByIdRspDTO>> response = userFeignApi.findByIds(findUsersByIdsReqDTO);

        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }

        return response.getData();
    }

}
