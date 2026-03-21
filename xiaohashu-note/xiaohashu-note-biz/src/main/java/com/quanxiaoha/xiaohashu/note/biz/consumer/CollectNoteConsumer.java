package com.quanxiaoha.xiaohashu.note.biz.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.note.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteCollectionDO;
import com.quanxiaoha.xiaohashu.note.biz.domain.mapper.NoteCollectionDOMapper;
import com.quanxiaoha.xiaohashu.note.biz.model.dto.CollectNoteDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_COLLECT_NOTE , // Group
        topic = MQConstants.TOPIC_COLLECT_NOTE, // 消费的主题 Topic
        consumeMode = ConsumeMode.ORDERLY )
public class CollectNoteConsumer implements RocketMQListener<Message> {
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private NoteCollectionDOMapper noteCollectionDOMapper;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Message message) {
        rateLimiter.acquire();
        log.info("===========consumer get collect\\uncollect message");
        String tag = message.getTags();
        String body = new String(message.getBody());
        switch (tag) {
            case MQConstants.TAG_COLLECT-> handleCollectMessage(body);
            case MQConstants.TAG_UNCOLLECT-> handleUncollectMessage(body);
        }
    }

    private void handleUncollectMessage(String body) {
        log.info("======get message to store Uncollect info to database");
        CollectNoteDTO collectNoteDTO = JsonUtils.Parse_Object(body, CollectNoteDTO.class);
        NoteCollectionDO collectionDO = NoteCollectionDO.builder()
                .userId(collectNoteDTO.getUserId())
                .noteId(collectNoteDTO.getNoteId())
                .createTime(collectNoteDTO.getCreateTime())
                .status(collectNoteDTO.getType())
                .build();
        int count = noteCollectionDOMapper.update2UncollectByUserIdAndNoteId(collectionDO);
        if(count>0){
            log.info("==========save uncollect message info into database");
            org.springframework.messaging.Message<String> message =  MessageBuilder.withPayload(body).build();
            rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_COLLECT,message,new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("=====send message to count uncollect successfully");
                }

                @Override
                public void onException(Throwable throwable) {
                    log.info("=====send message to count collect unsuccessfully");
                }
            });
        }
    }

    private void handleCollectMessage(String body) {
        log.info("======get message to store Uncollect info to database");
        CollectNoteDTO collectNoteDTO = JsonUtils.Parse_Object(body, CollectNoteDTO.class);
        int count = noteCollectionDOMapper.insertOrUpdate(collectNoteDTO);
        if(count>0){
            log.info("==========save collect message info into database successfully");
            org.springframework.messaging.Message<String> message =  MessageBuilder.withPayload(body).build();
            rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_COLLECT,message,new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("=====send message to count collect successfully");
                }

                @Override
                public void onException(Throwable throwable) {
                    log.info("=====send message to count collect unsuccessfully");
                }
            });
        }


    }
}
