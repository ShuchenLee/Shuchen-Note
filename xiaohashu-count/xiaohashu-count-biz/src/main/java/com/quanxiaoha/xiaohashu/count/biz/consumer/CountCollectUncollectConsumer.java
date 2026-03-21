package com.quanxiaoha.xiaohashu.count.biz.consumer;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.phantomthief.collection.BufferTrigger;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.count.biz.enums.CollectUncollectEnum;
import com.quanxiaoha.xiaohashu.count.biz.enums.LikeUnlikeEnum;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_COUNT_NOTE_COLLECT, // Group
        topic = MQConstants.TOPIC_COUNT_NOTE_COLLECT // 消费的主题 Topic
)
public class CountCollectUncollectConsumer implements RocketMQListener<String> {
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;
    @Resource
    private TaskExecutor taskExecutor;
    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeMessage)
            .build();


    private void consumeMessage(List<String> stringList) {
        log.info("==> 【笔记点赞数】聚合消息, size: {}", stringList.size());
        //distinct by userId
        List<CountCollectUncollectDTO> list = stringList.stream()
                .map(str -> JsonUtils.Parse_Object(str, CountCollectUncollectDTO.class)).toList();
        Map<Long, List<CountCollectUncollectDTO>> countMap = list.stream().collect(Collectors.groupingBy(CountCollectUncollectDTO::getNoteId));
        List<AggregationCountCollectUncollectNoteMqDTO> countList = new ArrayList<>();
        countMap.forEach((noteId, countCollectUncollectDTOList) -> {
            int count = 0;
            Long creatorId = null;
            for (CountCollectUncollectDTO countCollectUncollectDTO : countCollectUncollectDTOList) {
                creatorId = countCollectUncollectDTO.getCreatorId();
                if(ObjectUtil.isNull(countCollectUncollectDTO)) continue;
                switch(LikeUnlikeEnum.valueOf(countCollectUncollectDTO.getType())){
                    case LIKE -> count ++;
                    case UNLIKE -> count --;
                }
            }
            countList.add(AggregationCountCollectUncollectNoteMqDTO.builder()
                    .creatorId(creatorId)
                    .noteId(noteId)
                    .count(count)
                    .build());
        });
        //update redis
        countList.forEach(item->{
            Long creatorId = item.getCreatorId();
            Long noteId = item.getNoteId();
            Integer count = item.getCount();
            //update note count data
            String noteCountRedisKey = RedisConstants.buildNoteCountKey(noteId);
            if(BooleanUtil.isTrue(redisTemplate.hasKey(noteCountRedisKey)))
                redisTemplate.opsForHash().increment(noteCountRedisKey,RedisConstants.collectTotal,count);
            //update user count data
            String userCountRedisKey = RedisConstants.buildUserCountKey(creatorId);
            if(BooleanUtil.isTrue(redisTemplate.hasKey(userCountRedisKey)))
                redisTemplate.opsForHash().increment(userCountRedisKey,RedisConstants.collectTotal,count);
        });
        //update database
        taskExecutor.execute(()->{
            sendMQ(countList);
        });

    }

    private void sendMQ(List<AggregationCountCollectUncollectNoteMqDTO> countList) {
        //send message to store fans count into database;
        org.springframework.messaging.Message<String> message = MessageBuilder
                .withPayload(JsonUtils.toJsonString(countList)).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_COLLECT_DB,message,new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("======send collect note count message to database successfully");
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("======send uncollect note count message to database failed");
            }
        });
    }

    @Override
    public void onMessage(String s) {
        bufferTrigger.enqueue(s);
    }
}
