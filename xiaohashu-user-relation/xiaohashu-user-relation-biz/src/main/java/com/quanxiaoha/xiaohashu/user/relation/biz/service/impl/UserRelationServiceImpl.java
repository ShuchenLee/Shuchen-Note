package com.quanxiaoha.xiaohashu.user.relation.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.quanxiaoha.xiaohashu.common.exception.BizException;
import com.quanxiaoha.xiaohashu.common.response.PageResponse;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.common.util.DateUtils;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.context.holder.ContextHolder;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserIdRespDTO;
import com.quanxiaoha.xiaohashu.user.relation.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.user.relation.biz.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.dataobject.FansDO;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.dataobject.FollowingDO;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.mapper.FansDOMapper;
import com.quanxiaoha.xiaohashu.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.quanxiaoha.xiaohashu.user.relation.biz.enums.LuaResultEnum;
import com.quanxiaoha.xiaohashu.user.relation.biz.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.dto.FollowUserDTO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.dto.UnfollowUserDTO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.FansListReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.FollowListReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.FollowUserReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req.UnfollowUserReqVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.resp.FansListRespVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.resp.FollowListRespVO;
import com.quanxiaoha.xiaohashu.user.relation.biz.rpc.UserRpcService;
import com.quanxiaoha.xiaohashu.user.relation.biz.service.UserRelationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class UserRelationServiceImpl implements UserRelationService {
    @Resource
    private UserRpcService userRpcService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private FollowingDOMapper followingDOMapper;
    @Resource
    private FansDOMapper fansDOMapper;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public Response<?> followUser(FollowUserReqVO followUserReqVO) {
        //get user id
        Long userId = ContextHolder.getUserId();
        //get follower id
        Long followUserId = followUserReqVO.getFollowUserId();
        if(Objects.equals(userId,followUserId)){
            throw new BizException(ResponseCodeEnum.CANT_FOLLOW_YOUR_SELF);
        }
        //get follower
        FindByUserIdRespDTO respDTO = userRpcService.findById(followUserId);
        if(Objects.isNull(respDTO)){
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }
        //if the follower's fans reach limit
        //get redis key
        String redisKey = RedisConstants.buildUserFollowingKey(userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_add.lua")));
        // 返回值类型
        script.setResultType(Long.class);
        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        // 当前时间转时间戳
        long timeStamp = DateUtils.getTimeTamp(now);

        // 执行 Lua 脚本，拿到返回结果
        Long result = (Long)redisTemplate.execute(script, Collections.singletonList(redisKey), followUserId, timeStamp);
        LuaResultEnum luaResultEnum = LuaResultEnum.valueOf((result));
        if(Objects.isNull(luaResultEnum)){
            throw new RuntimeException("=========lua 脚本返回结果错误");
        }
        switch (luaResultEnum){
            case ZSET_NOT_EXIST ->{
                //get follower info from database
                List<FollowingDO> followingDOList = followingDOMapper.selectByUserId(userId);
                long expireSecs = 60*60^24 + RandomUtil.randomLong(60*60^24);
                if (CollectionUtil.isEmpty(followingDOList)) {
                    //if follow relationship is empty use lua script to add a zset
                    //invoke lua script
                    DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
                    script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_add_and_expire.lua")));
                    script2.setResultType(Long.class);
                    now = LocalDateTime.now();
                    timeStamp = DateUtils.getTimeTamp(now);
                    redisTemplate.execute(script2, Collections.singletonList(redisKey), followUserId, timeStamp,expireSecs);
                    log.info("=========在redis中新建该角色的关注列表");
                }else{
                    // 构建 Lua 参数
                    Object[] luaArgs = getLuaArgs(followingDOList, expireSecs);
                    // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
                    DefaultRedisScript<Long> script3 = new DefaultRedisScript<>();
                    script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_and_expire.lua")));
                    script3.setResultType(Long.class);
                    redisTemplate.execute(script3, Collections.singletonList(redisKey), luaArgs);
                    now = LocalDateTime.now();
                    timeStamp = DateUtils.getTimeTamp(now);
                    // 再次调用上面的 Lua 脚本：follow_check_and_add.lua , 将最新的关注关系添加进去
                    result = (Long)redisTemplate.execute(script, Collections.singletonList(redisKey), followUserId, timeStamp);
                    checkLuaResult(result);
                }
            }
            case FOLLOW_LIMIT -> {
                throw new BizException(ResponseCodeEnum.FOLLOWING_COUNT_LIMIT);
            }
            case ALREADY_FOLLOWED -> {
                throw new BizException(ResponseCodeEnum.ALREADY_FOLLOWED);
            }
            case FOLLOW_SUCCESS -> {
                break;
            }
        }
        //user message queue to write into database
        //create transport follow relationship info
        FollowUserDTO followingUserDO = FollowUserDTO.builder()
                .userId(userId)
                .followingUserId(followUserId)
                .createTime(now)
                .build();
        //user message queue
        //build message object
        String hashKey = String.valueOf(userId);
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(followingUserDO)).build();
        String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" +MQConstants.TAG_FOLLOW;
        log.info("=======ready to send message");
        rocketMQTemplate.asyncSendOrderly(destination,message,hashKey,new SendCallback(){
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("=====send message success");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("=====send message fail");
            }
        });
        return Response.success();
    }

    @Override
    public Response<?> unfollowUser(UnfollowUserReqVO unfollowUserReqVO) {
        //get userId
        Long userId = ContextHolder.getUserId();
        //get unfollowUserId
        Long unfollowUserId =  unfollowUserReqVO.getUnfollowUserId();
        if(Objects.equals(userId,unfollowUserId)){
            throw new BizException(ResponseCodeEnum.CANT_UNFOLLOW_SELF);
        }
        //check if unfollow user exist
        FindByUserIdRespDTO unfollowUser = userRpcService.findById(unfollowUserId);
        if(Objects.isNull(unfollowUser)){
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }
        //get follow relation from redis
        String redisKey = RedisConstants.buildUserFollowingKey(userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/check_follow_relation.lua")));
        script.setResultType(Long.class);
        Long result = (Long)redisTemplate.execute(script,Collections.singletonList(redisKey),unfollowUserId);
        switch (LuaResultEnum.valueOf(result)){
            case ZSET_NOT_EXIST ->{
                log.info("get this user follow relation list from database to redis");
                List<FollowingDO> followingDOList = followingDOMapper.selectByUserId(userId);
                if(followingDOList.isEmpty()){
                    throw  new BizException(ResponseCodeEnum.NOT_FOLLOWED);
                }
                else{
                    long expireSecs = 60*60^24 + RandomUtil.randomLong(60*60^24);
                    Object[] luaArgs = getLuaArgs(followingDOList, expireSecs);
                    // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
                    DefaultRedisScript<Long> script1 = new DefaultRedisScript<>();
                    script1.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_and_expire.lua")));
                    script1.setResultType(Long.class);
                    redisTemplate.execute(script1, Collections.singletonList(redisKey), luaArgs);
                    result = (Long)redisTemplate.execute(script, Collections.singletonList(redisKey), unfollowUserId);
                    checkLuaResult(result);

                }


            }
            case UNFOLLOW_SUCCESS -> log.info("unfollow success");
            case NOT_FOLLOW -> throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
        }
        //user message queue to delete relation from database
        //create transport follow relation info
        UnfollowUserDTO unfollowUserDTO = UnfollowUserDTO.builder()
                .userId(userId)
                .unfollowingUserId(unfollowUserId)
                .createTime(LocalDateTime.now())
                .build();
        //user message queue
        //build message object
        String hashKey = String.valueOf(userId);
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(unfollowUserDTO)).build();
        String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" +MQConstants.TAG_UNFOLLOW;
        log.info("=======ready to send unfollow message");
        rocketMQTemplate.asyncSendOrderly(destination,message, hashKey,new SendCallback(){

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("=====send unfollow message success");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("=====send unfollow message fail");
            }
        });
        return Response.success();
    }

    @Override
    public PageResponse<FollowListRespVO> getFollowList(FollowListReqVO followListReqVO) {
        //get user id
        Long userId = followListReqVO.getUserId();
        //get pageNO
        long pageNo = followListReqVO.getPageNo();
        //get following list
        //if exist in redis
        String redisKey = RedisConstants.buildUserFollowingKey(userId);
        List<FollowListRespVO> followListRespVOS = null;
        long pageSize = 10;
        long totalCount = redisTemplate.opsForZSet().zCard(redisKey);
        long totalPage = (totalCount + pageSize - 1) / pageSize;
        if(totalCount > 0){
            //the user following list info exist in redis
            //if requested pageNo is valid
            if(pageNo <= totalPage){
                //prepare return data
                long startIndex = (pageNo-1)*pageSize;
                //get data from redis
                Set<Object> set = redisTemplate.opsForZSet().reverseRangeByScore(redisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, startIndex, pageSize);
                if(CollUtil.isNotEmpty(set)){
                    List<Long> userIdList = set.stream()
                            .map(o->Long.valueOf(o.toString()))
                            .toList();
                    log.info("=========get user following list from redis");
                    followListRespVOS = getFollowListRespVOS(userIdList, followListRespVOS);
                }
            }else{
                return PageResponse.success(null, pageNo, totalCount);
            }
        }else{
            //search the user following list info from database
            //get all follow relationship in database
            totalCount = followingDOMapper.count(userId);
            totalPage = (totalCount + pageSize - 1) / pageSize;
            if(pageNo > totalPage) return PageResponse.success(null, pageNo, totalCount);
            long startIndex = (pageNo-1)*pageSize;
            List<FollowingDO> followingDOs = followingDOMapper.selectPageListByUserId(userId, startIndex, pageSize);
            if(ObjectUtil.isNotEmpty(followingDOs)){
                List<Long> userIdList = followingDOs.stream().map(FollowingDO::getFollowingUserId).toList();
                followListRespVOS = getFollowListRespVOS(userIdList, followListRespVOS);
                //put this user's all follow relation in redis
                threadPoolTaskExecutor.submit(()->syncPutFollowingIntoRedis(userId));
            }

        }

        return PageResponse.success(followListRespVOS,pageNo,totalCount);
    }

    @Override
    public PageResponse<FansListRespVO> getFansList(FansListReqVO fansListReqVO) {
        //get user id
        Long userId = fansListReqVO.getUserId();
        Long pageNo = fansListReqVO.getPageNo();
        //get fans info from redis
        String redisKey = RedisConstants.buildUserFansKey(userId);
        long totalCount = (long)redisTemplate.opsForZSet().zCard(redisKey);
        long pageSize = 10L;
        long totalPage = (totalCount + pageSize - 1) / pageSize;
        List<FansListRespVO> fanListResult = null;
        if(totalCount > 0){
            if(pageNo <= totalPage){
                //if pageNo valid
                //get all fans id
                long startIndex = (pageNo-1) * pageSize;
                Set<Object> redisResult = redisTemplate.opsForZSet().reverseRangeByScore(redisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, startIndex, pageSize);
                if(CollUtil.isNotEmpty(redisResult)){
                    List<Long> fansIdList = redisResult.stream().map(p -> Long.valueOf(p.toString())).toList();
                    log.info("=========get user fans list from redis");
                    fanListResult = getFansListRespVOS(fansIdList,fanListResult);
                }
            }else{
                //if pageNo invalid
                return PageResponse.success(null,pageNo, totalPage);
            }

        }else{
            //search the user fans list info from database
            //get all fans relationship in database
            totalCount = fansDOMapper.count(userId);
            totalPage = (totalCount + pageSize - 1) / pageSize;
            if(pageNo > totalPage) return PageResponse.success(null, pageNo, totalCount);
            long startIndex = (pageNo-1)*pageSize;
            List<FansDO> fansDOs = fansDOMapper.selectPageListByUserId(userId, startIndex, pageSize);
            if(ObjectUtil.isNotEmpty(fansDOs)){
                List<Long> userIdList = fansDOs.stream().map(FansDO::getFansUserId).toList();
                fanListResult = getFansListRespVOS(userIdList, fanListResult);
                //put this user's all fans relation in redis
                threadPoolTaskExecutor.submit(()->syncPutFansIntoRedis(userId));
            }
        }

        return PageResponse.success(fanListResult,pageNo,totalCount);
    }

    private void syncPutFansIntoRedis(Long userId) {
        log.info("========sync put {} all fans relation into redis",userId);
        //get all follow relation from database
        List<FansDO> fansDOList = fansDOMapper.selectByUserId(userId);
        if (ObjectUtil.isNotEmpty(fansDOList)){
            String redisKey = RedisConstants.buildUserFansKey(userId);
            long expireSecs = 60*60^24 + RandomUtil.randomLong(60*60^24);
            // 构建 Lua 参数
            Object[] luaArgs = getFansLuaArgs(fansDOList, expireSecs);
            // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(redisKey), luaArgs);
        }
    }

    private Object[] getFansLuaArgs(List<FansDO> fansDOList, long expireSecs) {
        int resultLength =  fansDOList.size()*2+1;
        Object[] result = new Object[resultLength];
        int i = 0;
        for (FansDO fansDO:fansDOList){
            result[i++] = DateUtils.getTimeTamp(fansDO.getCreateTime());
            result[i++] = fansDO.getFansUserId();
        }
        result[resultLength-1] =  expireSecs;
        return result;
    }

    private void syncPutFollowingIntoRedis(Long userId) {

        //get all follow relation from database
        List<FollowingDO> followingDOList = followingDOMapper.selectByUserId(userId);
        if (ObjectUtil.isNotEmpty(followingDOList)){
            log.info("========sync put {} all follow relation into redis",userId);
            String redisKey = RedisConstants.buildUserFollowingKey(userId);
            long expireSecs = 60*60^24 + RandomUtil.randomLong(60*60^24);
            // 构建 Lua 参数
            Object[] luaArgs = getLuaArgs(followingDOList, expireSecs);
            // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(redisKey), luaArgs);
        }

    }

    private List<FollowListRespVO> getFollowListRespVOS(List<Long> userIdList, List<FollowListRespVO> followListRespVOS) {
        List<FindByUserIdRespDTO> userInfos = userRpcService.findByIds(userIdList);
        if(CollUtil.isNotEmpty(userInfos)){
            followListRespVOS = userInfos.stream().map(
                    dto-> FollowListRespVO.builder()
                            .userId(dto.getUserId())
                            .avatar(dto.getAvatar())
                            .introduction(dto.getIntroduction())
                            .nickname(dto.getNickName())
                            .build()
            ).toList();
        }
        return followListRespVOS;
    }

    private List<FansListRespVO> getFansListRespVOS(List<Long> userIdList, List<FansListRespVO> fansListRespVOs) {
        List<FindByUserIdRespDTO> userInfos = userRpcService.findByIds(userIdList);
        if(CollUtil.isNotEmpty(userInfos)){
            fansListRespVOs = userInfos.stream().map(
                    dto-> FansListRespVO.builder()
                            .userId(dto.getUserId())
                            .avatar(dto.getAvatar())
                            .introduction(dto.getIntroduction())
                            .nickname(dto.getNickName())
                            .build()
            ).toList();
        }
        return fansListRespVOs;
    }

    private void checkLuaResult(Long result) {
        LuaResultEnum luaResultEnum = LuaResultEnum.valueOf((result));
        switch(luaResultEnum){
            case FOLLOW_LIMIT -> throw new BizException(ResponseCodeEnum.FOLLOWING_COUNT_LIMIT);
            case ALREADY_FOLLOWED -> throw new BizException(ResponseCodeEnum.ALREADY_FOLLOWED);
            case NOT_FOLLOW ->  throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
            case UNFOLLOW_SUCCESS -> log.info("======unfollow success");
        }
    }

    private Object[] getLuaArgs(List<FollowingDO> followingDOList,Long expireSecs) {
        int resultLength =  followingDOList.size()*2+1;
        Object[] result = new Object[resultLength];
        int i = 0;
        for (FollowingDO followingDO:followingDOList){
            result[i++]=DateUtils.getTimeTamp(followingDO.getCreateTime());
            result[i++] = followingDO.getFollowingUserId();
        }
        result[resultLength-1] =  expireSecs;
        return result;
    }
}
