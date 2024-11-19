package com.arnanzz.kv.biz.domain.service;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.kv.dto.req.AddNoteContentReqDTO;
import com.arnanzz.kv.dto.req.DeleteNoteContentReqDTO;
import com.arnanzz.kv.dto.req.FindNoteContentReqDTO;
import com.arnanzz.kv.dto.resp.FindNoteContentRspDTO;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
public interface NoteContentService {

    /**
     * 添加笔记内容
     */
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);

    /**
     * 查询笔记内容
     */
    Response<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO);

    /**
     * 删除笔记内容
     */
    Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO);

}
