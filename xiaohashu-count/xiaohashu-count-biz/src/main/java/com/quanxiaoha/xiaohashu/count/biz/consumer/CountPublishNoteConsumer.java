package com.quanxiaoha.xiaohashu.count.biz.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.github.phantomthief.collection.BufferTrigger;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.UserCountDOMapper;
import com.quanxiaoha.xiaohashu.count.biz.enums.PublishUnpublishNoteEnum;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountPublishNoteDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_COUNT_PUBLISH, // Group
        topic = MQConstants.TOPIC_COUNT_PUBLISH // 消费的主题 Topic
)
public class CountPublishNoteConsumer implements RocketMQListener<Message> {
    @Resource
    private RateLimiter rateLimiter;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserCountDOMapper userCountDOMapper;

    @Override
    public void onMessage(Message message) {
//        if(true) return ;

        log.info("=======get message to handle publish\\unpublish count");
        String body = new String(message.getBody());
        String tag = message.getTags();
        switch (tag) {
            case MQConstants.TAG_PUBLISH -> handlePublishMessage(body);
            case MQConstants.TAG_UNPUBLISH -> handleUnpublishMessage(body);
        }
    }
    private void handlePublishMessage(String body) {
        log.info("=======get message to handle publish count");
        CountPublishNoteDTO countPublishNoteDTO = JsonUtils.Parse_Object(body, CountPublishNoteDTO.class);
        String userCountRedisKey = RedisConstants.buildUserCountKey(countPublishNoteDTO.getUserId());
        Boolean b = redisTemplate.hasKey(userCountRedisKey);
        if(ObjectUtil.equal(b,true)) redisTemplate.opsForHash().increment(userCountRedisKey,RedisConstants.publishTotal,1);
        int i = userCountDOMapper.insertOrUpdatePublishNote(countPublishNoteDTO.getUserId(), 1);
        if(i >= 0) log.info("=======save publish message count info to database successfully");
    }
    private void handleUnpublishMessage(String body) {
        log.info("=======get message to handle unpublish count");
        CountPublishNoteDTO countPublishNoteDTO = JsonUtils.Parse_Object(body, CountPublishNoteDTO.class);
        String userCountRedisKey = RedisConstants.buildUserCountKey(countPublishNoteDTO.getUserId());
        Boolean b = redisTemplate.hasKey(userCountRedisKey);
        if(ObjectUtil.equal(b,true)) redisTemplate.opsForHash().increment(userCountRedisKey,RedisConstants.publishTotal,-1);
        userCountDOMapper.insertOrUpdatePublishNote(countPublishNoteDTO.getUserId(),-1);
    }
}
