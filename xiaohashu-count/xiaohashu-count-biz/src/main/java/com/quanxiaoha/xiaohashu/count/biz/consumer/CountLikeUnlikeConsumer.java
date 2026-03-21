package com.quanxiaoha.xiaohashu.count.biz.consumer;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.phantomthief.collection.BufferTrigger;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.count.biz.enums.LikeUnlikeEnum;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.AggregationCountLikeUnlikeNoteMqDTO;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountLikeUnlikeDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_COUNT_NOTE_LIKE, // Group
        topic = MQConstants.TOPIC_COUNT_NOTE_LIKE // 消费的主题 Topic
    )
public class CountLikeUnlikeConsumer implements RocketMQListener<String> {
    private final RedisTemplate<String, Object> redisTemplate;
    private final TaskExecutor taskExecutor;
    private final RocketMQTemplate rocketMQTemplate;
    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeMessage)
            .build();

    public CountLikeUnlikeConsumer(RedisTemplate<String, Object> redisTemplate, TaskExecutor taskExecutor, RocketMQTemplate rocketMQTemplate) {
        this.redisTemplate = redisTemplate;
        this.taskExecutor = taskExecutor;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void onMessage(String message) {
        bufferTrigger.enqueue(message);
    }
    private void consumeMessage(List<String> strings) {
        log.info("==> 【笔记点赞数】聚合消息, size: {}", strings.size());
        log.info("==> 【笔记点赞数】聚合消息, {}", JsonUtils.toJsonString(strings));
        List<CountLikeUnlikeDTO> list = strings.stream().map(str -> JsonUtils.Parse_Object(str, CountLikeUnlikeDTO.class)).toList();
        Map<Long, List<CountLikeUnlikeDTO>> groupMap = list.stream().collect(Collectors.groupingBy(CountLikeUnlikeDTO::getNoteId));
        List<AggregationCountLikeUnlikeNoteMqDTO> countList = new ArrayList<>();
        groupMap.forEach((noteId, countLikeUnlikeDTOList) -> {
            int count = 0;
            Long creatorId = null;
            for (CountLikeUnlikeDTO countLikeUnlikeDTO : countLikeUnlikeDTOList) {
                creatorId = countLikeUnlikeDTO.getCreatorId();
                if(ObjectUtil.isNull(countLikeUnlikeDTO)) continue;
                switch(LikeUnlikeEnum.valueOf(countLikeUnlikeDTO.getType())){
                    case LIKE -> count ++;
                    case UNLIKE -> count --;
                }
            }
            countList.add(AggregationCountLikeUnlikeNoteMqDTO.builder()
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
                redisTemplate.opsForHash().increment(noteCountRedisKey,RedisConstants.likeTotal,count);
            //update user count data
            String userCountRedisKey = RedisConstants.buildUserCountKey(creatorId);
            if(BooleanUtil.isTrue(redisTemplate.hasKey(userCountRedisKey)))
                redisTemplate.opsForHash().increment(userCountRedisKey,RedisConstants.likeTotal,count);
        });
        //update database
        taskExecutor.execute(()->{
            sendMQ(countList);
        });

    }

    private void sendMQ(List<AggregationCountLikeUnlikeNoteMqDTO> countList) {
        //send message to store fans count into database;
        org.springframework.messaging.Message<String> message = MessageBuilder
                .withPayload(JsonUtils.toJsonString(countList)).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_LIKE_DB,message,new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("======send like note count message to database successfully");
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("======send like note count message to database failed");
            }
        });

    }


}
