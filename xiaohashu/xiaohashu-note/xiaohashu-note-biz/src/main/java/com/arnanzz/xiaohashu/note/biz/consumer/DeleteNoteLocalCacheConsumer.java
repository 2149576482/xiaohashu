package com.arnanzz.xiaohashu.note.biz.consumer;

import com.arnanzz.xiaohashu.note.biz.constant.MQConstants;
import com.arnanzz.xiaohashu.note.biz.domain.service.NoteDOService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author ArnanZZ
 * @version 1.0
 * @description: 删除本地笔记缓存
 **/
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group", //Group
topic = MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, // 消费的主题
messageModel = MessageModel.BROADCASTING) // 广播模式
public class DeleteNoteLocalCacheConsumer implements RocketMQListener<String> {

    @Resource
    private NoteDOService noteDOService;

    @Override
    public void onMessage(String s) {
        Long noteId = Long.valueOf(s);
        log.info("## 删除本地笔记缓存，noteId: {}", noteId);
        noteDOService.deleteNoteLocalCache(noteId);
    }
}
