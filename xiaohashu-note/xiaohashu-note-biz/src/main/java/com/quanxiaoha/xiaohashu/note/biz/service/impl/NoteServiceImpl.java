package com.quanxiaoha.xiaohashu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.quanxiaoha.xiaohashu.common.exception.BizException;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.common.util.DateUtils;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.context.holder.ContextHolder;
import com.quanxiaoha.xiaohashu.note.biz.constants.MQConstants;
import com.quanxiaoha.xiaohashu.note.biz.constants.RedisConstants;
import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteCollectionDO;
import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteDO;
import com.quanxiaoha.xiaohashu.note.biz.domain.dataobject.NoteLikeDO;
import com.quanxiaoha.xiaohashu.note.biz.domain.mapper.NoteCollectionDOMapper;
import com.quanxiaoha.xiaohashu.note.biz.domain.mapper.NoteDOMapper;
import com.quanxiaoha.xiaohashu.note.biz.domain.mapper.NoteLikeDOMapper;
import com.quanxiaoha.xiaohashu.note.biz.domain.mapper.TopicDOMapper;
import com.quanxiaoha.xiaohashu.note.biz.enums.*;
import com.quanxiaoha.xiaohashu.note.biz.model.dto.CollectNoteDTO;
import com.quanxiaoha.xiaohashu.note.biz.model.dto.CountPublishNoteDTO;
import com.quanxiaoha.xiaohashu.note.biz.model.dto.LikeNoteDTO;
import com.quanxiaoha.xiaohashu.note.biz.model.vo.req.*;
import com.quanxiaoha.xiaohashu.note.biz.model.vo.resp.GetNoteDetailRespVO;
import com.quanxiaoha.xiaohashu.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.quanxiaoha.xiaohashu.note.biz.rpc.KeyValueRpcService;
import com.quanxiaoha.xiaohashu.note.biz.rpc.UserRpcService;
import com.quanxiaoha.xiaohashu.note.biz.service.NoteService;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserIdRespDTO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NoteServiceImpl implements NoteService {
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Resource
    private NoteDOMapper noteDOMapper;
    @Resource
    private TopicDOMapper topicDOMapper;
    @Autowired
    private UserRpcService userRpcService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    //线程池
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;
    private static final Cache<Long, String> caffeine = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(1, TimeUnit.HOURS) // 设置缓存条目在写入后 1 小时过期
            .build();
    @Autowired
    private NoteLikeDOMapper noteLikeDOMapper;
    @Autowired
    private NoteCollectionDOMapper noteCollectionDOMapper;

    @Override
    public Response<?> publishNote(PublishNoteVO publishNoteVO) {
        //if note type valid
        Integer noteType = publishNoteVO.getType();
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.getNoteType(noteType);
        if(Objects.isNull(noteTypeEnum)){
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROE);
        }
        LocalDateTime createTime = LocalDateTime.now();
        //check images and video
        String images= null;
        String video = null;
        switch (noteTypeEnum){
            case IMAGE_TEXT -> {
                List<String> imageUrlList = publishNoteVO.getImage();
                Preconditions.checkArgument(CollUtil.isNotEmpty(imageUrlList),"笔记图片不能为空");
                Preconditions.checkArgument(imageUrlList.size() <= 8,"图片数量不能大于8");
                images = StringUtils.join(imageUrlList,",");
            }
            case VIDEO -> {
                String videoUrl = publishNoteVO.getVideo();
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUrl),"笔记视频不能为空");
                video = videoUrl;
            }
        }
        //invoke rpc service to generate noteId
        String noteId = distributedIdGeneratorRpcService.getSnowflakeId();
        //generate content key-value uuid
        String contentId = null;
        boolean isContentEmpty = true;
        String content = publishNoteVO.getContent();
        //save note content
        if(StringUtils.isNotBlank(content)){
            contentId = UUID.randomUUID().toString();
            isContentEmpty = false;
            if(!keyValueRpcService.saveNoteContent(contentId,publishNoteVO.getContent())){
                throw new BizException(ResponseCodeEnum.NOTE_PUBLISH_ERROR);
            }
        }
        //get topic name
        Long topicId = publishNoteVO.getTopicId();
        String topicName = null;
        if(Objects.nonNull(topicId)){
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);
        }
        //get creator id
        Long creatorId = (Long)ContextHolder.getUserId();
        //generate note do
        NoteDO noteDO = NoteDO.builder()
                .id(Long.valueOf(noteId))
                .topicId(topicId)
                .topicName(topicName)
                .title(publishNoteVO.getTitle())
                .isContentEmpty(isContentEmpty)
                .creatorId(creatorId)
                .isTop(false)
                .type(noteType)
                .imgUris(images)
                .videoUri(video)
                .visible(NoteVisibleEnum.PUBLIC.getCode())
                .createTime(createTime)
                .updateTime(createTime)
                .status(NoteStatusEnum.NORMAL.getCode())
                .contentUuid(contentId)
                .build();
        //try to store note
        try{
            noteDOMapper.insert(noteDO);
        }catch (Exception e){
            log.error("笔记存储失败，{}",e);
        }
        CountPublishNoteDTO countPublicNoteDTO = CountPublishNoteDTO.builder()
                .userId(creatorId)
                .noteId(Long.valueOf(noteId))
                .createTime(createTime)
                .type(PublishNoteEnum.PUBLISH.getType())
                .build();
        String countPublishNoteDestination = MQConstants.TOPIC_COUNT_PUBLISH + ":" + MQConstants.TAG_PUBLISH;
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countPublicNoteDTO)).build();
        rocketMQTemplate.asyncSend(countPublishNoteDestination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("======send message to count publish note successfully");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("======send message to count publish note unsuccessfully");
            }
        });
        return Response.success();
    }
    @Override
    @SneakyThrows
    public Response<GetNoteDetailRespVO> getNoteDetail(GetNoteDetailReqVO getNoteDetailReqVO) {
        //get note id
        Long noteId = getNoteDetailReqVO.getId();
        //get note detail info from caffeine
        String noteDetailCache = caffeine.getIfPresent(noteId);
        if(Objects.nonNull(noteDetailCache)){
            log.info("==========从本地缓存中读取笔记{}的数据",noteId);
            return Response.success(JsonUtils.Parse_Object(noteDetailCache,GetNoteDetailRespVO.class));
        }
        //get note detail info from redis
        String redisKey = RedisConstants.buildNoteDetailKey(noteId);
        String noteDetailRedisStr = (String)redisTemplate.opsForValue().get(redisKey);
        if(StringUtils.isNotBlank(noteDetailRedisStr)){
            GetNoteDetailRespVO noteDetailRedis = JsonUtils.Parse_Object(noteDetailRedisStr,GetNoteDetailRespVO.class);
            log.info("==========从Redis中读取笔记{}的数据",noteId);
            taskExecutor.execute(()->{
                log.info("==========从Redis向缓存写笔记{}的数据",noteId);
                caffeine.put(noteId,JsonUtils.toJsonString(noteDetailRedis));
            });
            return Response.success(noteDetailRedis);
        }
        //get note detail from database
        //get note info
        NoteDO noteDO = noteDOMapper.selectByPrimaryKey(noteId);
        if(Objects.isNull(noteDO)){
            throw new BizException(ResponseCodeEnum.NOTE_NOT_EXIST);
        }
        //check if note visible to current user
        Long currentUserId = (Long)ContextHolder.getUserId();
        Integer visibleType = noteDO.getVisible();
        Long creatorId = noteDO.getCreatorId();
        boolean flag = checkIfVisible(visibleType, currentUserId, creatorId);
        if(!flag){
            throw new BizException(ResponseCodeEnum.NOTE_PRIVATE);
        }
        //use async way to invoke rpc service
        //get user info
        FindByUserIdRespDTO userInfo = userRpcService.findUserById(creatorId);
        CompletableFuture<FindByUserIdRespDTO> userResultFuture = CompletableFuture
                .supplyAsync(()->userRpcService.findUserById(creatorId),taskExecutor);
        //user rpc service to get note content
        String content = null;
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        if(Objects.equals(noteDO.getIsContentEmpty(),Boolean.FALSE)){
            contentResultFuture = CompletableFuture
                    .supplyAsync(()->keyValueRpcService.getNodeContent(noteDO.getContentUuid()),taskExecutor);
        }
        //use async way to construct result
        CompletableFuture<String> finalContentResultFuture = contentResultFuture;
        CompletableFuture<GetNoteDetailRespVO> finalResultFuture = CompletableFuture
                .allOf(userResultFuture, contentResultFuture)
                .thenApply(s->{
                    FindByUserIdRespDTO asyncUserResult = userResultFuture.join();
                    String asyncContentResult = finalContentResultFuture.join();
                    Integer type = noteDO.getType();
                    String imgStr = noteDO.getImgUris();
                    List<String> imgUris = null;
                    if(Objects.equals(type,NoteTypeEnum.IMAGE_TEXT.getCode())){
                        imgUris = List.of(imgStr.split(","));
                    }
                    //construct result
                    return GetNoteDetailRespVO.builder()
                            .id(noteId)
                            .type(type)
                            .title(noteDO.getTitle())
                            .content(content)
                            .imgUris(imgUris)
                            .topicId(noteDO.getTopicId())
                            .topicName(noteDO.getTopicName())
                            .creatorId(noteDO.getCreatorId())
                            .creatorName(userInfo.getNickName())
                            .avatar(userInfo.getAvatar())
                            .videoUri(noteDO.getVideoUri())
                            .updateTime(noteDO.getUpdateTime())
                            .visible(noteDO.getVisible())
                            .build();
                });
        //get note type
        GetNoteDetailRespVO getNoteDetailRespVO = finalResultFuture.join();
        taskExecutor.execute(()->{
            log.info("========将笔记{}的信息写入Redis",noteId);
            Long expireSeconds = 60*60*24L + RandomUtil.randomInt(60*60*24);
            redisTemplate.opsForValue().set(redisKey,JsonUtils.toJsonString(getNoteDetailRespVO) ,expireSeconds, TimeUnit.SECONDS);
        });
        return Response.success(getNoteDetailRespVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO) {
        //get note id
        Long noteId = updateNoteReqVO.getNoteId();
        // 当前登录用户 ID
        Long currUserId = ContextHolder.getUserId();
        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);
        if(Objects.isNull(selectNoteDO)){
            throw new BizException(ResponseCodeEnum.NOTE_NOT_EXIST);
        }
        if(Objects.equals(selectNoteDO.getCreatorId(),currUserId)){
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }
        //if type valid
        Integer type = updateNoteReqVO.getType();
        if(!NoteTypeEnum.isValid(type)){
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROE);
        }
        //resolve images or video info
        String images = null;
        String video = null;
        switch(NoteTypeEnum.getNoteType(type)){
            case IMAGE_TEXT -> {
                List<String> imageUrisList = updateNoteReqVO.getImgUris();
                Preconditions.checkArgument(CollUtil.isNotEmpty(imageUrisList),"笔记图片不能为空");
                Preconditions.checkArgument(imageUrisList.size() <= 8,"图片数量不能大于8");
                images = StringUtils.join(imageUrisList,",");
            }
            case VIDEO -> {
                String videoUri = updateNoteReqVO.getVideoUri();
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri),"笔记视频不能为空");
                video = videoUri;
            }
        }

        //get topic name
        Long topicId = updateNoteReqVO.getTopicId();
        String topicName = null;
        if(Objects.nonNull(topicId)){
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);
            if(StringUtils.isBlank(topicName)) throw new BizException(ResponseCodeEnum.TOPIC_NOT_FOUND);
        }
        //construct noteDo to update
        String content = updateNoteReqVO.getContent();
        boolean isContentEmpty = StringUtils.isBlank(content);
        //update content in cassandra
        String uuid = noteDOMapper.selectByPrimaryKey(noteId).getContentUuid();
        String newUuid = null;
        boolean contentUpdateSuccess = true;
        if(isContentEmpty){
            keyValueRpcService.deleteNoteContent(uuid);
        }else{
            newUuid = UUID.randomUUID().toString();
            contentUpdateSuccess = keyValueRpcService.saveNoteContent(newUuid,content);
        }
        if(!contentUpdateSuccess){
            throw new BizException(ResponseCodeEnum.NOTE_UPDATE_ERROR);
        }
        //delete redis and cache
        String redisKey = RedisConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(redisKey);
        uuid = newUuid;
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .isContentEmpty(StringUtils.isBlank(content))
                .imgUris(images)
                .title(updateNoteReqVO.getTitle())
                .topicId(updateNoteReqVO.getTopicId())
                .topicName(topicName)
                .type(type)
                .updateTime(LocalDateTime.now())
                .videoUri(video)
                .contentUuid(uuid)
                .build();
        noteDOMapper.updateByPrimaryKeySelective(noteDO);


        caffeine.invalidate(noteId);
        //use message queue to delete all data
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        //use message queue to realize delay-delete
        Message<String> message = MessageBuilder.withPayload(redisKey).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("redis延迟删除成功 {}",redisKey);
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("redis延迟删除失败 {}",redisKey);
            }
        }, 3000, 1);
        return Response.success();
    }

    private boolean checkIfVisible(Integer visibleType,Long currentUserId,Long creatorId) {
        if(Objects.equals(visibleType,NoteVisibleEnum.PRIVATE.getCode()) && !Objects.equals(currentUserId,creatorId)){
            return false;
        }
        return true;
    }
    public void deleteCache(Long noteId){
        caffeine.invalidate(noteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> deleteNote(DeleteNoteReqVO deleteNoteReqVO) {
        //get note id
        Long noteId = deleteNoteReqVO.getNoteId();
        //check operation permission
        Long currUserId = ContextHolder.getUserId();
        LocalDateTime now = LocalDateTime.now();
        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);
        if(Objects.isNull(selectNoteDO)){
            throw new BizException(ResponseCodeEnum.NOTE_NOT_EXIST);
        }
        if(!Objects.equals(selectNoteDO.getCreatorId(),currUserId)){
            log.info("=============current userId {},note creator userId {}",currUserId,selectNoteDO.getCreatorId());
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }
        //logically delete
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .updateTime(now)
                .status(NoteStatusEnum.DELETED.getCode())
                .build();
        //delete redis cache
        String redisKey = RedisConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(redisKey);
        //delete local cache use message queue
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        CountPublishNoteDTO countPublicNoteDTO = CountPublishNoteDTO.builder()
                .userId(currUserId)
                .noteId(noteId)
                .createTime(now)
                .type(PublishNoteEnum.UNPUBLISH.getType())
                .build();
        String countPublishNoteDestination = MQConstants.TOPIC_COUNT_PUBLISH + ":" + MQConstants.TAG_UNPUBLISH;
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countPublicNoteDTO)).build();
        rocketMQTemplate.asyncSend(countPublishNoteDestination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("======send message to count unpublish note successfully");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("======send message to count unpublish note unsuccessfully");
            }
        });

        return Response.success();
    }

    @Override
    public Response<?> setNotePrivate(SetNotePrivateReqVO setNotePrivateReqVO) {
        //get note id
        Long noteId = setNotePrivateReqVO.getNoteId();
        Long currUserId = ContextHolder.getUserId();
        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);
        if(Objects.isNull(selectNoteDO)){
            throw new BizException(ResponseCodeEnum.NOTE_NOT_EXIST);
        }
        if(Objects.equals(selectNoteDO.getCreatorId(),currUserId)){
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }
        //construct update body
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .updateTime(LocalDateTime.now())
                .visible(NoteVisibleEnum.PRIVATE.getCode())
                .build();
        //update database
        if(noteDOMapper.updateNotePrivate(noteDO) <= 0){
            throw new BizException(ResponseCodeEnum.NOTE_SET_PRIVATE_ERROR);
        }
        //delete redis cache
        String redisKey = RedisConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(redisKey);
        //delete local cache
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        return Response.success();
    }

    @Override
    public Response<?> setNoteTop(SetNoteTopReqVO setNoteTopReqVO) {
        //get note id and top status
        Long noteId = setNoteTopReqVO.getNoteId();
        Boolean isTop = setNoteTopReqVO.getIsTop();
        //construct update body
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .isTop(isTop)
                .creatorId((Long)ContextHolder.getUserId())
                .updateTime(LocalDateTime.now())
                .build();
        if(noteDOMapper.updateNoteTop(noteDO) <= 0){
            throw new BizException(ResponseCodeEnum.NOTE_SET_TOP_ERROR);
        }
        //delete redis cache
        String redisKey = RedisConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(redisKey);
        //delete local cache use message queue
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        return Response.success();
    }

    @Override
    public Response<?> likeNote(LikeNoteReqVO likeNoteReqVO) {
        Long noteId = likeNoteReqVO.getNoteId();
        Long currUserId = ContextHolder.getUserId();
        //if note exists
        Long creatorId = checkNoteExist(noteId);
        LocalDateTime now = LocalDateTime.now();
        Long timeStamp = DateUtils.getTimeTamp(now);
        //if user have liked the note user bloom filter
        log.info("=====check and add bloom");
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        String redisKey = RedisConstants.buildBloomUserNoteLikeListKey(currUserId);
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_like_check.lua")));
        script.setResultType(Long.class);
        Long luaResult = (Long)redisTemplate.execute(script, Collections.singletonList(redisKey), noteId);
        LuaResultEnum luaResultEnum = LuaResultEnum.valueOf(luaResult);
        String likeListKey = RedisConstants.buildUserNoteLikesKey(currUserId);
        switch (luaResultEnum) {
            case NOTE_LIKED -> {
                //check redis liked list to avoid mistake
                Double score = redisTemplate.opsForZSet().score(likeListKey, noteId);
                if(score != null){
                    throw new BizException(ResponseCodeEnum.NOTE_HAVE_LIKED);
                }
                int count = noteLikeDOMapper.selectIfLikedByUserId(currUserId, noteId);
                if(count > 0){
                    initalLikeList(currUserId);
                    throw new BizException(ResponseCodeEnum.NOTE_HAVE_LIKED);
                }

            }
            case NOT_EXIST -> {
                log.info("========bloom not exist,create it");
                //get like info from database
                int count = noteLikeDOMapper.selectIfLikedByUserId(currUserId, noteId);
                if (count > 0) {
                    //if have like
                    initialBloom(currUserId);
                    throw new BizException(ResponseCodeEnum.NOTE_HAVE_LIKED);
                } else {
                    //not have like
                    //create bloom filter and add info
                    initialBloom(currUserId);
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    long expireSecs = 60 * 60 ^ 24 + RandomUtil.randomLong(60 * 60 ^ 24);
                    redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_create_and_add.lua")));
                    redisScript.setResultType(Long.class);
                    redisTemplate.execute(redisScript, Collections.singletonList(redisKey), noteId, expireSecs);
                }
            }
        }
        //update redis user like list
        LuaResultEnum likeListLuaResult = updateUserLikeList(likeListKey,noteId,timeStamp);
        if(likeListLuaResult == LuaResultEnum.NOT_EXIST){
            //get all like info from database
            List<NoteLikeDO> userLikeNoteList = noteLikeDOMapper.selectLikedByUserIdAndLimit(currUserId,100);
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            long expireSecs = 60 * 60 ^ 24 + RandomUtil.randomLong(60 * 60 ^ 24);
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/like_list_add_batch.lua")));
            redisScript.setResultType(Long.class);
            if(ObjectUtil.isNotEmpty(userLikeNoteList)){
                buildLikeListLuaArgs(userLikeNoteList,expireSecs);
                redisTemplate.execute(redisScript, Collections.singletonList(likeListKey), userLikeNoteList.toArray());
                updateUserLikeList(likeListKey,noteId,timeStamp);
            }else{
                List<Object> singleLuaArgs = new ArrayList<>();
                singleLuaArgs.add(timeStamp);
                singleLuaArgs.add(noteId);
                singleLuaArgs.add(expireSecs);
                redisTemplate.execute(redisScript, Collections.singletonList(likeListKey), singleLuaArgs.toArray());
            }
            log.info("======send MQ message to update database");

        }
        // put new like info into database todo
        LikeNoteDTO likeNoteDTO = LikeNoteDTO.builder()
                .userId(currUserId)
                .noteId(noteId)
                .type(LikeNoteEnum.LIKE.getType())
                .timeStamp(now)
                .creatorId(creatorId)
                .build();
        String hashKey = String.valueOf(currUserId);
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(likeNoteDTO)).build();
        String destination = MQConstants.TOPIC_LIKE_NOTE +":" + MQConstants.TAG_LIKE;
        rocketMQTemplate.asyncSendOrderly(destination, message,hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("======send like note message successfully");
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("======send like note message unsuccessfully");
            }
        });
        return Response.success();
    }

    public Response<?> unlikeNote(UnlikeNoteReqVO unlikeNoteReqVO) {
        //if note eixst
        Long userId = ContextHolder.getUserId();
        Long noteId = unlikeNoteReqVO.getNoteId();
        Long creatorId = checkNoteExist(noteId);
        LocalDateTime now = LocalDateTime.now();
        //if have liked
        log.info("=====check have liked");
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        String redisKey = RedisConstants.buildBloomUserNoteLikeListKey(userId);
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_unlike_check.lua")));
        script.setResultType(Long.class);
        Long luaResult = (Long)redisTemplate.execute(script, Collections.singletonList(redisKey), noteId);
        LuaResultEnum luaResultEnum = LuaResultEnum.valueOf(luaResult);
        String likeListKey = RedisConstants.buildUserNoteLikesKey(userId);
        switch(luaResultEnum){
            case NOT_LIKE -> {
                throw new BizException(ResponseCodeEnum.NOTE_HAVE_NOT_LIKED);
            }
            case NOT_EXIST->{
                log.info("========bloom not exist,create it");
                initialBloom(userId);
                //get like info from database
                int count = noteLikeDOMapper.selectIfLikedByUserId(userId, noteId);
                if (count <= 0) {
                    //if have like
                    throw new BizException(ResponseCodeEnum.NOTE_HAVE_NOT_LIKED);
                }
            }

        }
        //update zset
        String likeNoteListKey = RedisConstants.buildUserNoteLikesKey(userId);
        redisTemplate.opsForZSet().remove(likeNoteListKey, noteId);
        //update database
        LikeNoteDTO likeNoteDTO = LikeNoteDTO.builder()
                .userId(userId)
                .noteId(noteId)
                .type(LikeNoteEnum.UNLIKE.getType())
                .timeStamp(now)
                .creatorId(creatorId)
                .build();
        String hashKey = String.valueOf(userId);
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(likeNoteDTO)).build();
        String destination = MQConstants.TOPIC_LIKE_NOTE +":" + MQConstants.TAG_UNLIKE;
        rocketMQTemplate.asyncSendOrderly(destination, message,hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("======send unlike note message successfully");
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("======send unlike note message unsuccessfully");
            }
        });
        return Response.success();
    }

    @Override
    public Response<?> collectNote(CollectNoteReqVO collectNoteReqVO) {
        //get uerId and noteId
        Long userId = ContextHolder.getUserId();
        Long noteId = collectNoteReqVO.getNoteId();
        LocalDateTime now = LocalDateTime.now();
        //if note exist
        Long creatorId = checkNoteExist(noteId);
        //if have collected
        //first use bloom filter
        DefaultRedisScript<Long> bloomScript = new DefaultRedisScript<>();
        bloomScript.setResultType(Long.class);
        bloomScript.setScriptSource(new ResourceScriptSource(new  ClassPathResource("/lua/bloom_collect_check.lua")));
        String bloomRedisKey = RedisConstants.buildBloomUserNoteCollectKey(userId);
        String collectListRedisKey = RedisConstants.buildUserNoteCollectKey(userId);
        Long bloomCheckResult = (Long)redisTemplate.execute(bloomScript, Collections.singletonList(bloomRedisKey), noteId);
        CollectLuaResultEnum bloomCheckResultEnum = CollectLuaResultEnum.valueOf(bloomCheckResult);
        switch (bloomCheckResultEnum){
            case NOTE_COLLECTED -> {
                //bloom judge not have collect
                Double score = redisTemplate.opsForZSet().score(collectListRedisKey, noteId);
                if(ObjectUtils.isNotEmpty(score)){
                    throw new BizException(ResponseCodeEnum.NOTE_HAVE_COLLECTED);
                }
                int count = noteCollectionDOMapper.selectIfCollectd(userId, noteId);
                if(count > 0 ){
                    initialCollectRedisList(userId);
                    throw new BizException(ResponseCodeEnum.NOTE_HAVE_COLLECTED);
                }

            }
            case NOT_EXIST -> {
                //bloom not exist
                //check in database
                int count = noteCollectionDOMapper.selectIfCollectd(userId, noteId);
                initialNoteCollectionBloom(userId);
                if(count > 0){
                    //hava collected
                    throw new BizException(ResponseCodeEnum.NOTE_HAVE_COLLECTED);
                }else{
                    long expireSecs = 60*60*24 + RandomUtils.nextInt(0, 60*60*24);
                    DefaultRedisScript<Long> bloomScript1 = new DefaultRedisScript<>();
                    bloomScript1.setResultType(Long.class);
                    bloomScript1.setScriptSource(new  ResourceScriptSource(new ClassPathResource("/lua/bloom_collect_create_and_add.lua")));
                    redisTemplate.execute(bloomScript1, Collections.singletonList(bloomRedisKey), noteId,expireSecs);
                }
            }
        }
        //update user collection list in redis
        CollectLuaResultEnum updateRedisResult = updateCollectionList(userId,noteId,now);
        if(updateRedisResult == CollectLuaResultEnum.NOT_EXIST){
            initialCollectRedisList(userId);
            long expireSecs = 60 * 60 ^ 24 + RandomUtil.randomLong(60 * 60 ^ 24);
            redisTemplate.opsForZSet().add(collectListRedisKey,noteId, DateUtils.getTimeTamp(now));
            redisTemplate.expire(collectListRedisKey, expireSecs, TimeUnit.SECONDS);
        }
        //send MQ message to
        CollectNoteDTO collectNoteDTO = CollectNoteDTO.builder()
                .noteId(noteId)
                .userId(userId)
                .createTime(now)
                .creatorId(creatorId)
                .type(CollectNoteEnum.COLLECT.getType())
                .build();
        String hashKey = String.valueOf(userId);
        Message<String> message =  MessageBuilder.withPayload(JsonUtils.toJsonString(collectNoteDTO)).build();
        String destination = MQConstants.TOPIC_COLLECT_NOTE +":"+ MQConstants.TAG_COLLECT;
        rocketMQTemplate.asyncSendOrderly(destination, message , hashKey,new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("======send collect note message to database successfully");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("======send collect note message to database unsuccessfully");
            }
        });
        return Response.success();
    }

    @Override
    public Response<?> uncollectNote(UncollectNoteReqVO uncollectNoteReqVO) {
        //get userId noteId timestamp
        Long userId = ContextHolder.getUserId();
        Long noteId= uncollectNoteReqVO.getNoteId();
        LocalDateTime timestamp = LocalDateTime.now();
        String bloomRedisKey = RedisConstants.buildBloomUserNoteCollectKey(userId);
        String collectListRedisKey = RedisConstants.buildUserNoteCollectKey(userId);
        //check if note exist
        Long creatorId = checkNoteExist(noteId);
        //check if have collect
        DefaultRedisScript<Long> bloomScript = new DefaultRedisScript<>();
        bloomScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_uncollect_check.lua")));
        bloomScript.setResultType(Long.class);
        Long bloomResult = (Long)redisTemplate.execute(bloomScript,Collections.singletonList(bloomRedisKey),noteId);
        CollectLuaResultEnum bloomCheckResultEnum = CollectLuaResultEnum.valueOf(bloomResult);
        switch (bloomCheckResultEnum){
            case NOT_COLLECTED -> throw new BizException(ResponseCodeEnum.NOTE_HAVE_NOT_COLLECTED);
            case NOT_EXIST -> {
                //initial bloom
                initialNoteCollectionBloom(userId);
                int count = noteCollectionDOMapper.selectIfCollectd(userId, noteId);
                if(count <= 0) throw new BizException(ResponseCodeEnum.NOTE_HAVE_NOT_COLLECTED);
            }
        }
        //update zset
        redisTemplate.opsForZSet().remove(collectListRedisKey, noteId);
        //update database
        String hashKey = String.valueOf(userId);
        CollectNoteDTO collectNoteDTO = CollectNoteDTO.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(timestamp)
                .type(CollectNoteEnum.UNCOLLECT.getType())
                .creatorId(creatorId)
                .build();
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(collectNoteDTO)).build();
        String destination = MQConstants.TOPIC_COLLECT_NOTE +":"+ MQConstants.TAG_UNCOLLECT;
        rocketMQTemplate.asyncSendOrderly(destination, message , hashKey,new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("======send uncollect note message to database successfully");
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("======send uncollect note message to database unsuccessfully");
            }
        });
        return Response.success();
    }

    private CollectLuaResultEnum updateCollectionList(Long userId, Long noteId, LocalDateTime now) {
        Long timeStamp = DateUtils.getTimeTamp(now);
        DefaultRedisScript<Long> collectListScript = new DefaultRedisScript<>();
        collectListScript.setResultType(Long.class);
        collectListScript.setScriptSource(new ResourceScriptSource(new  ClassPathResource("/lua/collect_check_and_add.lua")));
        String collectListRedisKey = RedisConstants.buildUserNoteCollectKey(userId);
        Long result = (Long)redisTemplate.execute(collectListScript, Collections.singletonList(collectListRedisKey), timeStamp, userId);
        return CollectLuaResultEnum.valueOf(result);
    }

    /**
     * initial user collect list
     * @param userId
     */
    private void initialCollectRedisList(Long userId) {
        //get the user all note collection
        List<NoteCollectionDO> noteCollectionDOList = noteCollectionDOMapper.selectAllByUserIdLimit(userId);
        String redisKey = RedisConstants.buildUserNoteCollectKey(userId);
        Boolean exist = redisTemplate.hasKey(redisKey);
        if(!exist){
            long expireSecs = 60 * 60 ^ 24 + RandomUtil.randomLong(60 * 60 ^ 24);
            List<Long> luaArgs =  new ArrayList<>();
            if(CollUtil.isEmpty(noteCollectionDOList)) return ;
            noteCollectionDOList.forEach(noteCollectionDO -> {
                luaArgs.add(DateUtils.getTimeTamp(noteCollectionDO.getCreateTime()));
                luaArgs.add(noteCollectionDO.getNoteId());
            });
            luaArgs.add(expireSecs);
            DefaultRedisScript<Long> collectListScript = new DefaultRedisScript<>();
            collectListScript.setResultType(Long.class);
            collectListScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/collect_create_and_add_batch.lua")));
            redisTemplate.execute(collectListScript, Collections.singletonList(redisKey), luaArgs.toArray());
        }

    }

    /**
     * build note collection bloom filter
     * @param userId
     */
    private void initialNoteCollectionBloom(Long userId) {
        //get the user all note collection
        List<NoteCollectionDO> noteCollectionDOList = noteCollectionDOMapper.selectAllByUserId(userId);
        if (CollUtil.isEmpty(noteCollectionDOList)) {
            return ;
        }
        List<Long> noteIdList = noteCollectionDOList.stream().map(NoteCollectionDO::getNoteId).toList();
        String redisKey = RedisConstants.buildBloomUserNoteCollectKey(userId);

        List<Long> luaArgs =  new ArrayList<>();
        noteIdList.forEach(noteId -> {
            luaArgs.add(noteId);
        });
        long expireSecs = 60 * 60 ^ 24 + RandomUtil.randomLong(60 * 60 ^ 24);
        luaArgs.add(expireSecs);
        DefaultRedisScript<Long> bloomScript = new DefaultRedisScript<>();
        bloomScript.setResultType(Long.class);
        bloomScript.setScriptSource(new  ResourceScriptSource(new ClassPathResource("/lua/bloom_add_collection_batch.lua")));
        redisTemplate.execute(bloomScript, Collections.singletonList(redisKey), luaArgs.toArray());


    }


    private void initalLikeList(Long currUserId) {
        String redisKey = RedisConstants.buildUserNoteLikesKey(currUserId);
        boolean exist = redisTemplate.hasKey(redisKey);
        if(!exist){
            List<NoteLikeDO> userLikeNoteList = noteLikeDOMapper.selectLikedByUserIdAndLimit(currUserId,100);
            if(ObjectUtil.isEmpty(userLikeNoteList)) return ;
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            long expireSecs = 60 * 60 ^ 24 + RandomUtil.randomLong(60 * 60 ^ 24);
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/like_list_add_batch.lua")));
            redisScript.setResultType(Long.class);
            buildLikeListLuaArgs(userLikeNoteList,expireSecs);
            redisTemplate.execute(redisScript, Collections.singletonList(redisKey), userLikeNoteList);
        }
    }

    private List<Object> buildLikeListLuaArgs(List<NoteLikeDO> userLikeNoteList, long expireSecs) {
        List<Object> args = new ArrayList<>();
        for (NoteLikeDO noteLikeDO : userLikeNoteList){
            args.add(DateUtils.getTimeTamp(noteLikeDO.getCreateTime()));
            args.add(noteLikeDO.getNoteId());
        }
        args.add(expireSecs);
        return args;
    }

    private LuaResultEnum updateUserLikeList(String likeListKey,Long noteId, Long timeStamp) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/like_list_check_and_add.lua")));
        LuaResultEnum result = LuaResultEnum.valueOf((Long)redisTemplate.execute(redisScript, Collections.singletonList(likeListKey),noteId,timeStamp));
        return result;
    }

    private void initialBloom(Long userId) {
        taskExecutor.execute(()->{
            try{
                String redisKey = RedisConstants.buildUserNoteLikesKey(userId);
                List<NoteLikeDO> noteIdList = noteLikeDOMapper.selectAllByUserId(userId);
                Long expireSecs = 60*60^24 + RandomUtil.randomLong(60*60^24);
                if(CollUtil.isNotEmpty(noteIdList)){
                    List<Object> luaArgs = new ArrayList<>();
                    noteIdList.forEach(noteId -> {
                        luaArgs.add(noteId.getNoteId());
                    });
                    luaArgs.add(expireSecs);
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    redisScript.setResultType(Long.class);
                    redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_add_batch.lua")));
                    redisTemplate.execute(redisScript, Collections.singletonList(redisKey), luaArgs.toArray());
                }

            }catch (Exception e){
                log.error("initialBloom error",e);
            }

        });
    }

    private Long checkNoteExist(Long noteId) {
        //find in local cache
        String localCache = caffeine.getIfPresent(noteId);
        GetNoteDetailRespVO getNoteDetailRespVO = JsonUtils.Parse_Object(localCache, GetNoteDetailRespVO.class);
        if(Objects.isNull(getNoteDetailRespVO)){
            log.info("======note {} not in cache",noteId);
            String redisKey = RedisConstants.buildNoteDetailKey(noteId);
            String o = (String)redisTemplate.opsForValue().get(redisKey);
            getNoteDetailRespVO =  JsonUtils.Parse_Object(o,GetNoteDetailRespVO.class);
            if(Objects.isNull(getNoteDetailRespVO)){
                log.info("======note {} not in redis",noteId);
                Long creatorId = noteDOMapper.selectCreatorIdByNoteId(noteId);
                if(Objects.isNull(creatorId)){
                    throw new BizException(ResponseCodeEnum.NOTE_NOT_EXIST);
                }//put note info into redis and cache
                taskExecutor.execute(() -> {
                    GetNoteDetailReqVO getNoteDetailReqVO = GetNoteDetailReqVO
                            .builder()
                            .id(noteId)
                            .build();
                    getNoteDetail(getNoteDetailReqVO);
                });
                return creatorId;
            }

        }
        return getNoteDetailRespVO.getCreatorId();
    }
}



