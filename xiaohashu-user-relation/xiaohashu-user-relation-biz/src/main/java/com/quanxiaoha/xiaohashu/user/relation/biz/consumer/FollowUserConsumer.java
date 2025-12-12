package com.quanxiaoha.xiaohashu.user.relation.biz.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.quanxiaoha.xiaohashu.common.util.DateUtils;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.user.relation.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.user.relation.biz.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.dataobject.FansDO;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.dataobject.FollowingDO;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.mapper.FansDOMapper;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.quanxiaoha.xiaohashu.user.relation.biz.enums.CountFollowUnfollowEnum;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.dto.CountFollowUnfollowDTO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.dto.FollowUserDTO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.dto.UnfollowUserDTO;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group"+ MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW, // Group
        topic = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,
        consumeMode = ConsumeMode.ORDERLY) // 消费的主题 Topic) // 广播模式
public class FollowUserConsumer implements RocketMQListener<Message> {
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private FollowingDOMapper followingDOMapper;
    @Resource
    private FansDOMapper fansDOMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private RedisTemplate redisTemplate;
    //create token-bucket
    @Resource
    private RateLimiter rateLimiter;
    @Override
    public void onMessage(Message message) {
        //get token
        double acquire = rateLimiter.acquire();
        log.info("=======get token successfully {}",acquire);
        log.info("=======consumer already get message");
        //get message tag
        String tag = message.getTags();
        //get message body
        String body = new String(message.getBody());
        if(StringUtils.equals(tag,MQConstants.TAG_FOLLOW)){
            //add follow ship info
            log.info("=====add following info into database");
            handleFollow(body);
        }else if(StringUtils.equals(tag,MQConstants.TAG_UNFOLLOW)){
            //delete follow ship info
            log.info("======delete following relation from database");
            handleUnfollow(body);
        }else{
            throw new RuntimeException("关注操作类型错误");
        }

    }

    private void handleUnfollow(String body) {
        UnfollowUserDTO unfollowUserDTO = JsonUtils.Parse_Object(body,UnfollowUserDTO.class);
        if(Objects.isNull(unfollowUserDTO)){
            return ;
        }
        log.info("=========unfollowUserDTO is {}",unfollowUserDTO);
        Long userId = unfollowUserDTO.getUserId();
        Long unfollowUserId = unfollowUserDTO.getUnfollowingUserId();
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                int count = followingDOMapper.deleteFollowRelation(userId, unfollowUserId);
                if (count > 0) {
                    fansDOMapper.deleteFanRelation(unfollowUserId, userId);
                }
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error(e.getMessage(), e);
            }
            return false;
        }));
        log.info("=======delete follow relation from database {}",isSuccess);
        if(isSuccess){
            log.info("=======delete fan from redis");
            String redisKey = RedisConstants.buildUserFansKey(unfollowUserId);
            redisTemplate.opsForZSet().remove(redisKey,userId);
        }
        CountFollowUnfollowDTO countFollowUnfollowDTO = CountFollowUnfollowDTO.builder()
                                                            .userId(userId)
                                                            .targetUserId(unfollowUserId)
                                                            .type(CountFollowUnfollowEnum.UNFOLLOW.getType())
                                                            .build();
        sendMQ(countFollowUnfollowDTO);
    }
    private void handleFollow(String body) {
        FollowUserDTO  followUserDTO = JsonUtils.Parse_Object(body, FollowUserDTO.class);
        if(followUserDTO==null){
            return ;
        }
        log.info("=========followUserDTO is {}",followUserDTO);
        Long userId = followUserDTO.getUserId();
        Long followingId = followUserDTO.getFollowingUserId();
        LocalDateTime timestamp = followUserDTO.getCreateTime();
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try{
                //add following info into following database
                int count = followingDOMapper.insert(FollowingDO.builder()
                        .userId(userId).followingUserId(followingId)
                        .createTime(timestamp).build());
                if(count>0){
                    fansDOMapper.insert(FansDO.builder()
                            .userId(followingId).fansUserId(userId)
                            .createTime(timestamp).build());
                }
                return true;
            }catch (Exception e){
                status.setRollbackOnly();
                log.error("",e);
            }
            return false;
            }
                ));
        log.info("=======add following info into database {}",isSuccess);
        //update following user fans cache
        if(isSuccess){
            log.info("=======add new fans info  into redis");
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_and_add_fans.lua")));
            // 返回值类型
            script.setResultType(Long.class);
            String fansRedisKey = RedisConstants.buildUserFansKey(followingId);
            long timeStamp = DateUtils.getTimeTamp(LocalDateTime.now());
            redisTemplate.execute(script, Collections.singletonList(fansRedisKey), userId, timeStamp);
            CountFollowUnfollowDTO countFollowUnfollowDTO = CountFollowUnfollowDTO.builder()
                    .userId(userId)
                    .targetUserId(followingId)
                    .type(CountFollowUnfollowEnum.FOLLOW.getType())
                    .build();
            sendMQ(countFollowUnfollowDTO);
        }

    }
    private void sendMQ(CountFollowUnfollowDTO countFollowUnfollowDTO) {
        String hashKey = String.valueOf(countFollowUnfollowDTO.getUserId());
        org.springframework.messaging.Message<String> message = MessageBuilder
                .withPayload(JsonUtils.toJsonString(countFollowUnfollowDTO)).build();
        rocketMQTemplate.asyncSendOrderly(MQConstants.TOPIC_COUNT_FOLLOWING,message,hashKey,new SendCallback(){
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("=====send count following message success {}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("=====send count fans message fail {}",throwable);
            }
        });
        rocketMQTemplate.asyncSendOrderly(MQConstants.TOPIC_COUNT_FANS,message,hashKey,new SendCallback(){
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("=====send count fans message success {}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("=====send count following message fail {}",throwable);
            }
        });
    }
}
