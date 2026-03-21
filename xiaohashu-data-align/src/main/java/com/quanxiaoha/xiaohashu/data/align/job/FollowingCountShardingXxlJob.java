package com.quanxiaoha.xiaohashu.data.align.job;

import cn.hutool.core.collection.CollUtil;
import com.quanxiaoha.xiaohashu.data.align.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.data.align.constants.TableConstants;
import com.quanxiaoha.xiaohashu.data.align.domain.mapper.DeleteMapper;
import com.quanxiaoha.xiaohashu.data.align.domain.mapper.SelectMapper;
import com.quanxiaoha.xiaohashu.data.align.domain.mapper.UpdateMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class FollowingCountShardingXxlJob {
    @Autowired
    private SelectMapper selectMapper;
    @Autowired
    private UpdateMapper updateMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private DeleteMapper deleteMapper;
    @XxlJob("FollowingCountShardingJobHandler")
    public void FollowingCountShardingJobHandler(String param) throws Exception {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("=======shardIndex:{},\tshardTotal:{}",shardIndex,shardTotal);
        String date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tableSuffix = TableConstants.buildTableNameSuffix(date, shardIndex);
        int batchSize = 1000;
        int total = 0;
        //get all follow increment user id by batch
        while(true){
            //get batch user id list
            List<Long> userIdList = selectMapper.selectBatchFromDataAlignFollowingCountTempTable(tableSuffix, batchSize);
            if(CollUtil.isEmpty(userIdList)){
                break;
            }
            //align data in current batch
            for(Long userId : userIdList){
                //get following count
                int followingCount = selectMapper.selectCountFromFollowingTableByUserId(userId);
                //update t_user_count and redis
                updateMapper.updateUserFollowingTotalByUserId(userId,followingCount);
                String redisHashKey = RedisConstants.buildUserCountPrefix(userId);
                redisTemplate.opsForHash().put(redisHashKey,RedisConstants.FOLLOW_TOTAL,followingCount);
            }
            //delete increment data in temp table
            deleteMapper.batchDeleteDataAlignFollowingCountTempTable(tableSuffix,userIdList);
            total += userIdList.size();
        }
        XxlJobHelper.log("=========align" + total + "data totally");

    }
}
