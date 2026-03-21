package com.quanxiaoha.xiaohashu.data.align.consumer;

import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.data.align.constants.MQConstants;
import com.quanxiaoha.xiaohashu.data.align.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.data.align.constants.TableConstants;
import com.quanxiaoha.xiaohashu.data.align.domain.mapper.InsertMapper;
import com.quanxiaoha.xiaohashu.data.align.model.dto.LikeUnlikeNoteMqDTO;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_data_align_" + MQConstants.TOPIC_COUNT_NOTE_LIKE, // Group
        topic = MQConstants.TOPIC_COUNT_NOTE_LIKE // 消费的主题 Topic
)
public class NoteLikeIncrementData2DBConsumer implements RocketMQListener<String> {
    @Value("${table.shards}")
    private int tableShards;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private InsertMapper insertMapper;
    @Override
    public void onMessage(String s) {
        log.info("=======get message to save note like increment to database ======");
        //parse message to dto to get essential info
        LikeUnlikeNoteMqDTO likeUnlikeNoteMqDTO = JsonUtils.Parse_Object(s, LikeUnlikeNoteMqDTO.class);
        if(Objects.isNull(likeUnlikeNoteMqDTO)){
            return ;
        }
        Long noteId = likeUnlikeNoteMqDTO.getNoteId();
        Long creatorId = likeUnlikeNoteMqDTO.getCreatorId();
        //prepare note like count list bloom check script,user like count list bloom check script and add bloom script
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_today_note_like_check.lua")));
        RedisScript<Long> bloomAddScript = RedisScript.of("return redis.call('BF.ADD', KEYS[1], ARGV[1])", Long.class);
        //resolve note like increment note count align
        String noteBloomKey = RedisConstants.buildBloomUserNoteLikeNoteIdListKey(date);
        Long noteBloomResult = redisTemplate.execute(redisScript,Collections.singletonList(noteBloomKey), noteId);
        if(Objects.equals(noteBloomResult,0L)){
            log.info("=======start to resolve note like increment note count align");
            String noteTableSuffix = TableConstants.buildTableNameSuffix(date, noteId % tableShards);
            try {
                insertMapper.insert2DataAlignNoteLikeCountTempTable(noteTableSuffix,noteId);
            } catch (Exception e) {
                log.error("===========",e);
            }
            redisTemplate.execute(bloomAddScript, Collections.singletonList(noteBloomKey), noteId);
        }
        //resolve note like increment creator count align
        String userBloomKey = RedisConstants.buildBloomUserNoteLikeUserIdListKey(date);
        Long userBloomResult = redisTemplate.execute(redisScript,Collections.singletonList(userBloomKey), creatorId);
        if(Objects.equals(userBloomResult,0L)){
            log.info("=======start to resolve note like increment creator count align");
            String userTableSuffix = TableConstants.buildTableNameSuffix(date, creatorId % tableShards);
            try {
                insertMapper.insert2DataAlignUserLikeCountTempTable(userTableSuffix,creatorId);
            } catch (Exception e) {
                log.error("===========",e);
            }
            redisTemplate.execute(bloomAddScript, Collections.singletonList(userBloomKey), creatorId);
        }
    }
}
