package com.arnanzz.xiaohashu.note.biz.rpc;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.kv.api.NoteContentFeignApi;
import com.arnanzz.xiaohashu.kv.dto.req.AddNoteContentReqDTO;
import com.arnanzz.xiaohashu.kv.dto.req.DeleteNoteContentReqDTO;
import com.arnanzz.xiaohashu.kv.dto.req.FindNoteContentReqDTO;
import com.arnanzz.xiaohashu.kv.dto.resp.FindNoteContentRspDTO;
import com.arnanzz.xiaohashu.note.biz.model.vo.FindNoteDetailRspVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: ID 自动生成服务调用
 **/
@Component
public class KeyValueRpcService {

    @Resource
    private NoteContentFeignApi noteContentFeignApi;

    /**
     * 添加笔记内容
     */
    public Boolean addNoteContent(String uuid, String content) {
        System.out.println("uuid type: " + uuid.getClass().getName());
        AddNoteContentReqDTO addNoteContentReqDTO = new AddNoteContentReqDTO();
        addNoteContentReqDTO.setNoteId(uuid);
        addNoteContentReqDTO.setContent(content);

        Response<?> response = noteContentFeignApi.addNoteContent(addNoteContentReqDTO);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return false;
        }
        return true;
    }

    /**
     * 查询笔记内容
     */
    public String findNoteContent(String uuid) {
        FindNoteContentReqDTO findNoteContentReqDTO = new FindNoteContentReqDTO();
        findNoteContentReqDTO.setNoteId(uuid);
        Response<FindNoteContentRspDTO> response = noteContentFeignApi.findNoteContent(findNoteContentReqDTO);

        if (Objects.isNull(response) || !response.isSuccess() || Objects.isNull(response.getData())) {
            return null;
        }
        return response.getData().getContent();
    }

    /**
     * 删除笔记内容
     */
    public boolean deleteNoteContent(String uuid) {
        DeleteNoteContentReqDTO deleteNoteContentReqDTO = DeleteNoteContentReqDTO.builder()
                .noteId(uuid).build();
        Response<?> response = noteContentFeignApi.deleteNoteContent(deleteNoteContentReqDTO);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return false;
        }
        return true;
    }
}
