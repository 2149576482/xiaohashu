package com.arnanzz.xiaohashu.note.biz.domain.service;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.note.biz.model.vo.*;

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

    /**
     * 删除本地笔记缓存
     */
    void deleteNoteLocalCache(Long noteId);

    /**
     * 删除笔记
     */
    Response<?> deleteNote(DeleteNoteReqVO deleteNoteReqVO);

    /**
     * 笔记仅对自己可见
     */
    Response<?> visibleOnlyMe(UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO);

    /**
     * 笔记置顶 / 取消置顶
     */
    Response<?> topNote(TopNoteReqVO topNoteReqVO);

}
