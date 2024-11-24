package com.arnanzz.xiaohashu.note.biz.domain.service;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.note.biz.model.vo.FindNoteDetailReqVO;
import com.arnanzz.xiaohashu.note.biz.model.vo.FindNoteDetailRspVO;
import com.arnanzz.xiaohashu.note.biz.model.vo.PublishNoteReqVO;
import com.arnanzz.xiaohashu.note.biz.model.vo.UpdateNoteReqVO;

public interface NoteDOService{

    /**
     * 笔记发布
     */
    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);

    /**
     * 笔记详情
     */
    Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO);

    /**
     * 笔记更新
     */
    Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO);

}
