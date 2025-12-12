package com.quanxiaoha.xiaohashu.count.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.UserCountDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group"+ MQConstants.TOPIC_COUNT_FANS_DB, // Group
        topic = MQConstants.TOPIC_COUNT_FANS_DB,
        consumeMode = ConsumeMode.ORDERLY)
public class CountFans2DBConsumer implements RocketMQListener<String> {
    @Resource
    private UserCountDOMapper userCountDOMapper;
    @Resource
    private RateLimiter rateLimiter;
    @Override
    public void onMessage(String message) {
        double acquire = rateLimiter.acquire();
        log.info("========get message to count fans into database {}",message);
        Map<Long,Integer> map = null;
        try{
            map = JsonUtils.parseMap(message,Long.class,Integer.class);
        }catch (Exception e){
            log.error("get message to count fans error",e);
        }
        if(CollUtil.isNotEmpty(map)){
            map.forEach((k,v)->{
                userCountDOMapper.insertOrUpdateFansTotalByUserId(k,v);
            });
        }
        log.info("========get message to count fans into database successfully");



    }
}
