package com.quanxiaoha.xiaohashu.data.align.consumer;

import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.data.align.constants.MQConstants;
import com.quanxiaoha.xiaohashu.data.align.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.data.align.constants.TableConstants;
import com.quanxiaoha.xiaohashu.data.align.domain.mapper.InsertMapper;
import com.quanxiaoha.xiaohashu.data.align.model.dto.FollowUserMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

@Component
@Slf4j
@RocketMQMessageListener(
        consumerGroup = "xiaohashu_group_data_align_" + MQConstants.TOPIC_COUNT_FOLLOWING,
        topic = MQConstants.TOPIC_COUNT_FOLLOWING
)
public class UserFollowIncrement2DBConsumer implements RocketMQListener<String> {
    @Value("${table.shards}")
    private int tableShards;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private InsertMapper insertRecordMapper;

    @Override
    public void onMessage(String s) {
        //parse message to dto and get essential info
        FollowUserMqDTO followUserMqDTO = JsonUtils.Parse_Object(s, FollowUserMqDTO.class);
        Long userId = followUserMqDTO.getFollowUserId();
        Long followerId = followUserMqDTO.getUserId();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_today_user_follow_check.lua")));
        //check if have resolve following increment data alignment
        String followBloomKey = RedisConstants.buildBloomUserFollowListKey(date);
        Long followBloomResult = redisTemplate.execute(redisScript, Collections.singletonList(followBloomKey), userId);
        if(Objects.equals(followBloomResult,0L)){
            //insert following increment data into database
            String followingTableSuffix = TableConstants.buildTableNameSuffix(date,userId % tableShards);
            log.info("===========start to insert today user following increment data into database");
            try{
                insertRecordMapper.insert2DataAlignFollowingCountTempTable(followingTableSuffix,userId);
            }catch (Exception e){
                log.error("=========",e);
            }
            redisTemplate.execute(
                    RedisScript.of("redis.call('BF.ADD',KEYS[1],ARGV[1])",Long.class),
                    Collections.singletonList(followBloomKey),userId
            );
        }
        String fansBloomKey = RedisConstants.buildBloomUserFansListKey(date);
        Long fansBloomResult = redisTemplate.execute(redisScript, Collections.singletonList(fansBloomKey), followerId);
        if(Objects.equals(fansBloomResult,0L)){
            //insert following increment data into database
            String followingTableSuffix = TableConstants.buildTableNameSuffix(date,followerId % tableShards);
            log.info("===========start to insert today user fans increment data into database");
            try {
                insertRecordMapper.insert2DataAlignFanscountTempTable(followingTableSuffix,userId);
            } catch (Exception e) {
                log.error("=========",e);
            }
            redisTemplate.execute(
                    RedisScript.of("redis.call('BF.ADD',KEYS[1],ARGV[1])",Long.class),
                    Collections.singletonList(fansBloomKey),followerId
            );
        }
        log.info("========resolve updating following and fans increment data alignment to database successfully");

    }
}
