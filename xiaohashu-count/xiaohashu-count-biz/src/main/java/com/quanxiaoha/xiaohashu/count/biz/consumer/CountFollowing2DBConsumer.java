package com.quanxiaoha.xiaohashu.count.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.UserCountDOMapper;
import com.quanxiaoha.xiaohashu.count.biz.enums.CountFollowUnfollowEnum;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.CountFollowUnfollowDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group"+ MQConstants.TOPIC_COUNT_FOLLOWING_DB, // Group
        topic = MQConstants.TOPIC_COUNT_FOLLOWING_DB,
        consumeMode = ConsumeMode.ORDERLY)
public class CountFollowing2DBConsumer implements RocketMQListener<String> {
    @Resource
    private UserCountDOMapper userCountDOMapper;
    @Resource
    private RateLimiter rateLimiter;
    @Override
    public void onMessage(String message) {
        double acquire = rateLimiter.acquire();
        log.info("========get message to count fans into database {}",message);
        if (StringUtils.isBlank(message)) return;

        CountFollowUnfollowDTO countFollowUnfollowMqDTO = JsonUtils.Parse_Object(message, CountFollowUnfollowDTO.class);

        // 操作类型：关注 or 取关
        Integer type = countFollowUnfollowMqDTO.getType();
        // 原用户ID
        Long userId = countFollowUnfollowMqDTO.getUserId();

        // 关注数：关注 +1， 取关 -1
        int count = Objects.equals(type, CountFollowUnfollowEnum.FOLLOW.getType()) ? 1 : -1;
        // 判断数据库中，若原用户的记录不存在，则插入；若记录已存在，则直接更新
        userCountDOMapper.insertOrUpdateFollowingTotalByUserId(userId, count);

    }
}
