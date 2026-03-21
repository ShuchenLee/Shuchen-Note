package com.quanxiaoha.xiaohashu.note.biz.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.note.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteLikeDO;
import com.quanxiaoha.xiaohashu.note.biz.domain.mapper.NoteLikeDOMapper;
import com.quanxiaoha.xiaohashu.note.biz.model.dto.LikeNoteDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_LIKE_NOTE, // Group
        topic = MQConstants.TOPIC_LIKE_NOTE, // 消费的主题 Topic
        consumeMode = ConsumeMode.ORDERLY )
public class LikeNoteConsumer implements RocketMQListener<Message> {
    @Resource
    private NoteLikeDOMapper  noteLikeDOMapper;
    @Resource
    private RateLimiter rateLimiter;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Message message) {
        //get token
        rateLimiter.acquire();
        String tags = message.getTags();
        String jsonStr =  new String(message.getBody());
        LikeNoteDTO likeNoteDTO = JsonUtils.Parse_Object(jsonStr, LikeNoteDTO.class);
        switch (tags) {
            case MQConstants.TAG_LIKE -> {
                log.info("========get like note message");
                handleLikeNote(likeNoteDTO);
            }

            case MQConstants.TAG_UNLIKE -> {
                log.info("========get unlike note message");
                handleUnlikeNote(likeNoteDTO);
            }
            default -> log.error("========get like note type error ");
        }
    }
    void handleLikeNote(LikeNoteDTO likeNoteDTO) {
        if (ObjectUtil.isNull(likeNoteDTO)) {
            return;
        }
        NoteLikeDO noteLikeDO = NoteLikeDO.builder()
                .noteId(likeNoteDTO.getNoteId())
                .userId(likeNoteDTO.getUserId())
                .createTime(likeNoteDTO.getTimeStamp())
                .status(likeNoteDTO.getType())
                .build();
        int count = noteLikeDOMapper.insertOrUpdate(noteLikeDO);
        log.info("========save like note successfully");
        if (count <= 0) return;
        org.springframework.messaging.Message<String> message = MessageBuilder
                .withPayload(JsonUtils.toJsonString(likeNoteDTO)).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE,message,new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========count like note successfully");
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("========count like note unsuccessfully");
            }
        });
    }


    void handleUnlikeNote(LikeNoteDTO likeNoteDTO){
        if(ObjectUtil.isNull(likeNoteDTO)){
            return ;
        }
        NoteLikeDO noteLikeDO = NoteLikeDO.builder()
                .noteId(likeNoteDTO.getNoteId())
                .userId(likeNoteDTO.getUserId())
                .createTime(likeNoteDTO.getTimeStamp())
                .status(likeNoteDTO.getType())
                .build();
        int count = noteLikeDOMapper.update2UnlikeByUserIdAndNoteId(noteLikeDO);
        log.info("========save unlike note successfully");
        if (count <= 0) return;
        org.springframework.messaging.Message<String> message = MessageBuilder
                .withPayload(JsonUtils.toJsonString(likeNoteDTO)).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE,message,new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========count unlike note successfully");
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("========count unlike note unsuccessfully");
            }
        });
    }
}
