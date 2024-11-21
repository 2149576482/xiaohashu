package com.arnanzz.xiaohashu.note.biz.domain.service;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.note.biz.model.vo.PublishNoteReqVO;

public interface NoteDOService{

    /**
     * 笔记发布
     */
    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);

}
