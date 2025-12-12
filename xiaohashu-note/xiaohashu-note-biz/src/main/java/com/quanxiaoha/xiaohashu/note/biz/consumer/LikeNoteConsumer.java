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
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
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
    void handleLikeNote(LikeNoteDTO likeNoteDTO){
        log.info("========save like note successfully");
    }
    void handleUnlikeNote(LikeNoteDTO likeNoteDTO){
        log.info("========save unlike note successfully");
    }
}
