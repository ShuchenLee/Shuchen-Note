package com.quanxiaoha.xiaohashu.count.biz.consumer;

import com.github.phantomthief.collection.BufferTrigger;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.count.biz.enums.CountFollowUnfollowEnum;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountFollowUnfollowDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group"+ MQConstants.TOPIC_COUNT_FANS, // Group
        topic = MQConstants.TOPIC_COUNT_FANS,
        consumeMode = ConsumeMode.ORDERLY)
public class CountFansConsumer implements RocketMQListener<String> {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RocketMQTemplate rocketMQTemplate;
    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeMessage)
            .build();

    public CountFansConsumer(RedisTemplate<String, Object> redisTemplate, RocketMQTemplate rocketMQTemplate) {
        this.redisTemplate = redisTemplate;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void onMessage(String body) {
        // 往 bufferTrigger 中添加元素
        bufferTrigger.enqueue(body);
    }
    private void consumeMessage(List<String> bodies) {
        log.info("==> 聚合消息, size: {}", bodies.size());
        List<CountFollowUnfollowDTO> list = bodies.stream().map(str -> JsonUtils.Parse_Object(str, CountFollowUnfollowDTO.class)).toList();
        Map<Long, List<CountFollowUnfollowDTO>> mapList = list.stream().collect(Collectors.groupingBy(CountFollowUnfollowDTO::getTargetUserId));
        Map<Long,Integer> countMap = new HashMap<>();
        for(Map.Entry<Long, List<CountFollowUnfollowDTO>> entry : mapList.entrySet()){
            Long userId = entry.getKey();
            int count = 0;
            List<CountFollowUnfollowDTO> countFollowUnfollowDTOList = entry.getValue();
            for(CountFollowUnfollowDTO c: countFollowUnfollowDTOList){
                Integer type = c.getType();
                CountFollowUnfollowEnum countFollowUnfollowEnum = CountFollowUnfollowEnum.valueOf(type);
                if(Objects.isNull(countFollowUnfollowEnum)) continue;
                switch (countFollowUnfollowEnum){
                    case FOLLOW-> count++;
                    case UNFOLLOW-> count--;
                }
            }
            countMap.put(userId, count);
        }
        countMap.forEach((userId, count) -> {
            String redisKey = RedisConstants.buildUserCountKey(userId);
            //if count hashkey exist
            Boolean b = redisTemplate.hasKey(redisKey);
            if(b){
                redisTemplate.opsForHash().increment(redisKey, RedisConstants.fansTotal, count);
            }
        });
        sendMQ(countMap);


    }

    private void sendMQ(Map<Long, Integer> countMap) {
        //send message to store fans count into database;
        org.springframework.messaging.Message<String> message = MessageBuilder
                .withPayload(JsonUtils.toJsonString(countMap)).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FANS_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========send count fans message success {}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("========send count fans message error {}",throwable);
            }
        });
    }
}
