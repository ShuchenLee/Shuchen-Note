package com.quanxiaoha.xiaohashu.count.biz.consumer;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.count.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.NoteCountDOMapper;
import com.quanxiaoha.xiaohashu.count.biz.domain.mapper.UserCountDOMapper;
import com.quanxiaoha.xiaohashu.count.biz.model.dto.AggregationCountLikeUnlikeNoteMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.support.JstlUtils;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group"+ MQConstants.TOPIC_COUNT_LIKE_DB, // Group
        topic = MQConstants.TOPIC_COUNT_LIKE_DB
)
public class CountNoteLike2DBConsumer implements RocketMQListener<String> {
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private  NoteCountDOMapper noteCountDOMapper;
    @Autowired
    private UserCountDOMapper userCountDOMapper;

    @Override
    public void onMessage(String s) {
        log.info("=====get message to save note like info into database");
        List<AggregationCountLikeUnlikeNoteMqDTO> countList = null;
        try{
            countList = JsonUtils.parseList(s, AggregationCountLikeUnlikeNoteMqDTO.class);
        }catch (Exception e){
            log.error("======get message to count note like error");
        }
        if(CollectionUtil.isNotEmpty(countList)){
            countList.forEach(item->{
                Long userId = item.getCreatorId();
                Long noteId = item.getNoteId();
                Integer count = item.getCount();
                transactionTemplate.execute(status -> {
                    try{
                        noteCountDOMapper.countNoteLike(noteId,count);
                        userCountDOMapper.insertOrUpdateLikeTotalByUserId(userId,count);
                    }catch (Exception e){
                        status.setRollbackOnly();
                        log.error("======get message to count note like error");
                    }
                    return false;
                });
            });
            log.info("=====get message to save note like info into database successfully");
        }


    }
}
