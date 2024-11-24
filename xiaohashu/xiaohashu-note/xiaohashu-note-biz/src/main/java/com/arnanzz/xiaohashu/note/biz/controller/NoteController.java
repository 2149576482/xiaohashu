package com.arnanzz.xiaohashu.note.biz.controller;

import com.arnanzz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.arnanzz.framework.common.response.Response;
import com.arnanzz.xiaohashu.note.biz.domain.service.NoteDOService;
import com.arnanzz.xiaohashu.note.biz.model.vo.FindNoteDetailReqVO;
import com.arnanzz.xiaohashu.note.biz.model.vo.FindNoteDetailRspVO;
import com.arnanzz.xiaohashu.note.biz.model.vo.PublishNoteReqVO;
import com.arnanzz.xiaohashu.note.biz.model.vo.UpdateNoteReqVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 笔记
 **/
@RestController
@RequestMapping("/note")
@Slf4j
public class NoteController {

    @Resource
    private NoteDOService noteDOService;

    /**
     * 笔记发布
     */
    @PostMapping(value = "/publish")
    @ApiOperationLog(description = "笔记发布")
    public Response<?> publishNote(@Validated @RequestBody PublishNoteReqVO publishNoteReqVO) {
        return noteDOService.publishNote(publishNoteReqVO);
    }

    /**
     * 查询笔记详情
     */
    @PostMapping(value = "/detail")
    @ApiOperationLog(description = "笔记详情")
    public Response<FindNoteDetailRspVO> findNoteDetail(@Validated @RequestBody FindNoteDetailReqVO findNoteDetailReqVO) {
        return noteDOService.findNoteDetail(findNoteDetailReqVO);
    }

    /**
     * 更新笔记
     */
    @PostMapping(value = "/update")
    @ApiOperationLog(description = "笔记修改")
    public Response<?> updateNote(@Validated @RequestBody UpdateNoteReqVO updateNoteReqVO) {
        return noteDOService.updateNote(updateNoteReqVO);
    }
}
