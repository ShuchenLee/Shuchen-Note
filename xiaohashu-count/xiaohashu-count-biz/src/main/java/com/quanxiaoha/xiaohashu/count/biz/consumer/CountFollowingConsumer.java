package com.quanxiaoha.xiaohashu.count.biz.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.github.phantomthief.collection.BufferTrigger;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.count.biz.enums.CountFollowUnfollowEnum;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountFollowUnfollowDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
@RocketMQMessageListener(consumerGroup = "xiaohashu_group"+ MQConstants.TOPIC_COUNT_FOLLOWING, // Group
        topic = MQConstants.TOPIC_COUNT_FOLLOWING,
        consumeMode = ConsumeMode.ORDERLY)
public class CountFollowingConsumer implements RocketMQListener<String> {

    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String message) {
        log.info("========get message to count following");
        log.info("## 消费到了 MQ 【计数: 关注数】, {}...", message);

        if (StringUtils.isBlank(message)) return;
        // 关注数和粉丝数计数场景不同，单个用户无法短时间内关注大量用户，所以无需聚合
        // 直接对 Redis 中的 Hash 进行 +1 或 -1 操作即可

        CountFollowUnfollowDTO countFollowUnfollowMqDTO = JsonUtils.Parse_Object(message, CountFollowUnfollowDTO.class);

        // 操作类型：关注 or 取关
        Integer type = countFollowUnfollowMqDTO.getType();
        // 原用户ID
        Long userId = countFollowUnfollowMqDTO.getUserId();

        // 更新 Redis
        String redisKey = RedisConstants.buildUserCountKey(userId);
        // 判断 Hash 是否存在
        boolean isExisted = redisTemplate.hasKey(redisKey);

        // 若存在
        if (isExisted) {
            // 关注数：关注 +1， 取关 -1
            long count = Objects.equals(type, CountFollowUnfollowEnum.FOLLOW.getType()) ? 1 : -1;
            // 对 Hash 中的 followingTotal 字段进行加减操作
            redisTemplate.opsForHash().increment(redisKey, RedisConstants.followTotal, count);
        }

        // 发送 MQ, 关注数写库
        // 构建消息对象
        org.springframework.messaging.Message<String> dbmessage = MessageBuilder.withPayload(message)
                .build();

        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FOLLOWING_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：关注数入库】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：关注数入库】MQ 发送异常: ", throwable);
            }
        });
    }

}
