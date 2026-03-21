package com.quanxiaoha.xiaohashu.data.align.consumer;

import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.data.align.constants.MQConstants;
import com.quanxiaoha.xiaohashu.data.align.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.data.align.constants.TableConstants;
import com.quanxiaoha.xiaohashu.data.align.domain.mapper.InsertMapper;
import com.quanxiaoha.xiaohashu.data.align.model.dto.PublishNoteMqDTO;
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
import java.util.Collections;
import java.util.Objects;

@Component
@Slf4j
@RocketMQMessageListener(
        consumerGroup = "xiaohashu_group_data_align_" + MQConstants.TOPIC_COUNT_PUBLISH,
        topic = MQConstants.TOPIC_COUNT_PUBLISH
)
public class NotePublishIncrementData2DBConsumer implements RocketMQListener<String> {
    @Value("${table.shards}")
    private int tableShards;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private InsertMapper insertRecordMapper;


    @Override
    public void onMessage(String s) {
        log.info("========start to deal with note publish data align task");
        //parse message string to dto and get essential info
        PublishNoteMqDTO publishNoteMqDTO = JsonUtils.Parse_Object(s, PublishNoteMqDTO.class);
        if(Objects.isNull(publishNoteMqDTO)){
            return ;
        }
        Long creatorId = publishNoteMqDTO.getUserId();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String bloomKey = RedisConstants.buildBloomUserNotePublishListKey(date);
        //user bloom to check if have recorded this note increment
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource
                (new ClassPathResource("/lua/bloom_today_note_publish_check.lua")));
        Long execute = redisTemplate.execute(redisScript, Collections.singletonList(bloomKey), creatorId);
        if(Objects.equals(execute,0L)){
            log.info("=========bloom today note publish list does not exist");
            //add note publish count user id into database
            String tableSuffix = TableConstants.buildTableNameSuffix(date,creatorId%tableShards);
            insertRecordMapper.insert2DataAlignUserNotePublishCountTempTable(tableSuffix,creatorId);
            redisTemplate.execute(
                    RedisScript.of("return redis.call('BF.ADD',KEYS[1],ARGV[1])",Long.class),
                    Collections.singletonList(bloomKey),
                    creatorId
            );
        }
        log.info("========deal with note publish data align task successfully");
    }
}
