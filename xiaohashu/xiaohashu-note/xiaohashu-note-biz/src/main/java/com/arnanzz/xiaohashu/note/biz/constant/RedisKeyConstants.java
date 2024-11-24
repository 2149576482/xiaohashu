package com.arnanzz.xiaohashu.note.biz.constant;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 统一 管理 redis Key
 **/
public class RedisKeyConstants {

    /**
     * 笔记详情 KEY 前缀
     */
    public static final String NOTE_DETAIL_KEY = "note:detail:";


    /**
     * 构建完整的笔记详情 KEY
     */
    public static String buildNoteDetailKey(Long noteId) {
        return NOTE_DETAIL_KEY + noteId;
    }
    
}
