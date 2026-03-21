package com.quanxiaoha.xiaohashu.count.biz.consumer;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.NoteCountDOMapper;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.UserCountDOMapper;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.AggregationCountCollectUncollectNoteMqDTO;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.AggregationCountLikeUnlikeNoteMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group"+ MQConstants.TOPIC_COUNT_COLLECT_DB, // Group
        topic = MQConstants.TOPIC_COUNT_COLLECT_DB
)
public class CountNoteCollect2DBConsumer implements RocketMQListener<String> {
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private  NoteCountDOMapper noteCountDOMapper;
    @Autowired
    private UserCountDOMapper userCountDOMapper;

    @Override
    public void onMessage(String s) {
        log.info("========save collect note count message to database");
        List<AggregationCountCollectUncollectNoteMqDTO> countList = null;
        try{
            countList = JsonUtils.parseList(s, AggregationCountCollectUncollectNoteMqDTO.class);
        }catch (Exception e){
            log.error("======get message to count note collect error");
        }
        if(CollectionUtil.isNotEmpty(countList)){
            countList.forEach(item->{
                Long userId = item.getCreatorId();
                Long noteId = item.getNoteId();
                Integer count = item.getCount();
                transactionTemplate.execute(status -> {
                    try{
                        noteCountDOMapper.countNoteCollect(noteId,count);
                        userCountDOMapper.insertOrUpdateCollectTotalByUserId(userId,count);
                    }catch (Exception e){
                        status.setRollbackOnly();
                        log.error("======get message to count note collect error");
                    }
                    return false;
                });
            });
            log.info("=====get message to save note collect info into database successfully");
        }

    }
}
