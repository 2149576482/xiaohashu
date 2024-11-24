package com.arnanzz.xiaohashu.note.biz.rpc;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.kv.api.NoteContentFeignApi;
import com.arnanzz.xiaohashu.kv.dto.req.AddNoteContentReqDTO;
import com.arnanzz.xiaohashu.kv.dto.req.DeleteNoteContentReqDTO;
import com.arnanzz.xiaohashu.user.api.UserFeignApi;
import com.arnanzz.xiaohashu.user.dto.req.FindUserByIdReqDTO;
import com.arnanzz.xiaohashu.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

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

}
