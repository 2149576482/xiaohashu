package com.arnanzz.xiaohashu.note.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description:
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteDetailRspVO {

    private Long id;
    private Integer type;
    private String title;
    private String content;
    private List<String> imgUris;
    private Long topicId;
    private String topicName;
    private Long creatorId;
    private String creatorName;
    private String avatar;
    private String videoUri;
    private LocalDateTime updateTime;
    private Integer visible;

}