package com.arnanzz.kv.api;

import com.arnanzz.framework.common.response.Response;
import com.arnanzz.kv.constant.ApiConstants;
import com.arnanzz.kv.dto.req.AddNoteContentReqDTO;
import com.arnanzz.kv.dto.req.DeleteNoteContentReqDTO;
import com.arnanzz.kv.dto.resp.FindNoteContentRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 调用用户服务接口
 **/
@FeignClient(name = ApiConstants.SERVER_NAME)
public interface NoteContentFeignApi {

    // 定义前缀
    String PREFIX = "/kv";

    /**
     * 添加笔记
     */
    @PostMapping(value = PREFIX + "/note/content/add")
    Response<?> addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);

    /**
     * 通过id获取笔记
     */
    @PostMapping(value = PREFIX + "/note/content/find")
    Response<FindNoteContentRspDTO> findNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);

    /**
     * 通过笔记id 删除笔记内容
     */
    @PostMapping(value = PREFIX + "/note/content/delete")
    Response<?> deleteNoteContent(@RequestBody DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}
