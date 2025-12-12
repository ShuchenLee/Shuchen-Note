package com.quanxiaoha.xiaohashu.user.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.github.benmanes.caffeine.cache.Cache;
import com.quanxiaoha.xiaohashu.common.enums.DeletedEnum;
import com.quanxiaoha.xiaohashu.common.enums.StatusEnum;
import com.quanxiaoha.xiaohashu.common.exception.BizException;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.common.util.ParamUtils;
import com.quanxiaoha.xiaohashu.context.holder.ContextHolder;
import com.quanxiaoha.xiaohashu.user.api.dto.req.*;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserIdRespDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserPhoneRespDTO;
import com.quanxiaoha.xiaohashu.user.biz.constant.RedisConstants;
import com.quanxiaoha.xiaohashu.user.biz.constant.RoleConstants;
import com.quanxiaoha.xiaohashu.user.biz.domain.dataobject.RoleDO;
import com.quanxiaoha.xiaohashu.user.biz.domain.dataobject.UserDO;
import com.quanxiaoha.xiaohashu.user.biz.domain.dataobject.UserRoleDO;
import com.quanxiaoha.xiaohashu.user.biz.domain.mapper.RoleDOMapper;
import com.quanxiaoha.xiaohashu.user.biz.domain.mapper.UserDOMapper;
import com.quanxiaoha.xiaohashu.user.biz.domain.mapper.UserRoleDOMapper;
import com.quanxiaoha.xiaohashu.user.biz.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.user.biz.enums.SexEnum;
import com.quanxiaoha.xiaohashu.user.biz.rpc.DistributedIdGeneratorService;
import com.quanxiaoha.xiaohashu.user.biz.vo.UpdateUserReqVO;
import com.quanxiaoha.xiaohashu.user.biz.rpc.OssRpcService;
import com.quanxiaoha.xiaohashu.user.biz.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource(name = "caffeineCache")
    private Cache<Long,FindByUserIdRespDTO> caffeineCache;
    @Autowired
    private RoleDOMapper roleDOMapper;
    @Autowired
    private UserRoleDOMapper userRoleDOMapper;
    @Autowired
    private OssRpcService ossRpcService;
    @Resource
    private DistributedIdGeneratorService distributedIdGeneratorService;
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public Response<?> updateUserInfo(UpdateUserReqVO updateUserReqVO) {
        //get login user id
        UserDO userDO = new UserDO();
        userDO.setId((Long) ContextHolder.getUserId());
        //if really can update
        boolean flag = false;
        //avatar
        MultipartFile avatar = updateUserReqVO.getAvatar();
        if(Objects.nonNull(avatar)) {
            //upload avatar
            String avatarUrl = ossRpcService.uploadFile(avatar);
            if(StringUtils.isBlank(avatarUrl)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }
            log.info("=======头像上传成功,地址为{}",avatarUrl);
            userDO.setAvatar(avatarUrl);
            flag = true;
        }
        //verify nickname
        String nickname = updateUserReqVO.getNickname();
        if(StringUtils.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamUtils.checkNickname(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL);
            userDO.setNickname(nickname);
            flag = true;
        }
        //verify xiaohashu num
        String xiaohashuId = updateUserReqVO.getXiaohashuId();
        if(StringUtils.isNotBlank(xiaohashuId)) {
            Preconditions.checkArgument(ParamUtils.checkXiaohashuId(xiaohashuId), ResponseCodeEnum.XIAOHASHU_ID_VALID_FAIL);
            userDO.setXiaohashuId(xiaohashuId);
            flag = true;
        }
        //verify sex
        Integer sex = updateUserReqVO.getSex();
        if(Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL);
            userDO.setSex(sex);
            flag = true;
        }
        //verify birthday
        LocalDate birthday = updateUserReqVO.getBirthday();
        if(Objects.nonNull(birthday)) {
            userDO.setBirthday(birthday);
            flag = true;
        }
        //verify introduction length
        String introduction = updateUserReqVO.getIntroduction();
        if(StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction,100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL);
            userDO.setIntroduction(introduction);
            flag = true;
        }
        //background image
        MultipartFile backgroundImage = updateUserReqVO.getBackgroundImg();
        if(Objects.nonNull(backgroundImage)) {
            //upload backgroun image
            String backgroundImageUrl = ossRpcService.uploadFile(backgroundImage);
            if(StringUtils.isBlank(backgroundImageUrl)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }
            log.info("=======头像上传成功,地址为{}",backgroundImageUrl);
            userDO.setBackgroundImg(backgroundImageUrl);
            flag = true;
        }
        if(flag){
            userDO.setUpdateTime(LocalDateTime.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);
        }
        return Response.success(userDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> registerUser(RegisterUserReqDTO registerUserReqDTO) {
        //get user phone
        String phone =  registerUserReqDTO.getPhone();
        //if have registered
        UserDO userDO1 = userDOMapper.selectByPhone(phone);
        if(Objects.nonNull(userDO1)) {
            log.info("==> 用户已经注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO1));
            return Response.success(userDO1.getId());
        }

        String xiaohashuId = distributedIdGeneratorService.getXiaohashuId();
        // RPC: 调用分布式 ID 生成服务生成用户 ID
        String userIdStr = distributedIdGeneratorService.getUserId();
        Long userId = Long.valueOf(userIdStr);
        UserDO userDO = UserDO.builder()
                .id(userId)
                .phone(phone)
                .xiaohashuId(String.valueOf(xiaohashuId))
                .nickname("小红薯" + xiaohashuId)
                .status(StatusEnum.ENABLE.getValue())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        userDOMapper.insert(userDO);
        // 给该用户分配一个默认角色
        UserRoleDO userRoleDO = UserRoleDO.builder()
                .userId(userId)
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        userRoleDOMapper.insert(userRoleDO);

        // 将该用户的角色 ID 存入 Redis 中
        RoleDO roleDO = roleDOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);

        // 将该用户的角色 ID 存入 Redis 中，指定初始容量为 1，这样可以减少在扩容时的性能开销
        List<String> roles = new ArrayList<>(1);
        roles.add(roleDO.getRoleKey());

        String userRolesKey = RedisConstants.buildUserRoleKey(userId);
        redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

        return Response.success(userId);

    }

    public Response<FindByUserPhoneRespDTO> findByPhone(FindByUserPhoneReqDTO findUserReqDTO) {
        //get phone
        String phone =  findUserReqDTO.getPhone();
        //get userDO
        UserDO userDO = userDOMapper.selectByPhone(phone);
        if(Objects.isNull(userDO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }
        FindByUserPhoneRespDTO result = FindByUserPhoneRespDTO.builder()
                                    .id(userDO.getId())
                                    .password(userDO.getPassword())
                                    .build();
        return Response.success(result);
    }

    @Override
    public Response<?> updatePassword(UpdatePasswordReqDTO updatePasswordReqDTO) {
        //get user id
        Long userId = (Long)ContextHolder.getUserId();
        //construct update entity
        UserDO userDO = UserDO.builder()
                .id(userId)
                .password(updatePasswordReqDTO.getNewPassword())
                .updateTime(LocalDateTime.now())
                .build();
        //update userDO info
        userDOMapper.updateByPrimaryKeySelective(userDO);
        return Response.success();
    }

    @Override
    public Response<FindByUserIdRespDTO> findUserById(FindByUserIdReqDTO findByUserIdReqDTO) {
        //get userId
        Long userId = findByUserIdReqDTO.getUserId();
        //get info from caffeine cache
        FindByUserIdRespDTO findByUserIdRespDTOCache = caffeineCache.getIfPresent(userId);
        if(Objects.nonNull(findByUserIdRespDTOCache)) {
            log.info("==========从本地缓存中读取用户{}的数据",userId);
            return Response.success(findByUserIdRespDTOCache);
        }
        //get user info from redis
        String userRedisKey = RedisConstants.buildUserInfoKey(userId);
        String userInfoValue = (String)redisTemplate.opsForValue().get(userRedisKey);
        if(StringUtils.isNotBlank(userInfoValue)) {
            log.info("========从Reids中读取到了用户{}的信息",userId);
            FindByUserIdRespDTO findByUserIdRespDTORedis = JsonUtils.Parse_Object(userInfoValue,FindByUserIdRespDTO.class);
            //add user info to caffeine cache
            threadPoolTaskExecutor.execute(() -> {
                caffeineCache.put(userId,findByUserIdRespDTORedis);
            });
            return Response.success(findByUserIdRespDTORedis);
        }
        //no userid info in redis
        // search user in database
        // no user in database
        UserDO userDO = userDOMapper.selectByPrimaryKey(userId);
        if(Objects.isNull(userDO)) {
            //store null object to redis to avoid cache through
            Long expireSeconds = 60L + RandomUtil.randomInt(60);
            threadPoolTaskExecutor.execute(()->{
                redisTemplate.opsForValue().set(userRedisKey, "null",expireSeconds, TimeUnit.SECONDS);
            });
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }
        //find user in database
        //construct result
        log.info("========从数据库中读取到了用户{}的信息",userId);
        FindByUserIdRespDTO findByUserIdRespDTO = FindByUserIdRespDTO.builder()
                .userId(userDO.getId())
                .nickName(userDO.getNickname())
                .avatar(userDO.getAvatar())
                .introduction(userDO.getIntroduction())
                .build();
        //save the user info into redis
        threadPoolTaskExecutor.execute(()->{
            log.info("========将用户{}的信息写入Redis",userId);
            Long expireSeconds = 60*60*24L + RandomUtil.randomInt(60*60*24);
            redisTemplate.opsForValue().set(userRedisKey,JsonUtils.toJsonString(findByUserIdRespDTO) ,expireSeconds, TimeUnit.SECONDS);
        });

        return Response.success(findByUserIdRespDTO);
    }

    @Override
    public Response<List<FindByUserIdRespDTO>> findUsersByIds(FindUsersByIdsReqDTO findUsersByIdsReqDTO) {
        //get id list
        List<Long> userId = findUsersByIdsReqDTO.getUserIdList();
        //get users info from redis
        List<String> redisKeyList = userId.stream().map(RedisConstants::buildUserInfoKey).collect(Collectors.toList());
        List<Object> redisResult = redisTemplate.opsForValue().multiGet(redisKeyList);
        if(Objects.nonNull(redisResult)) {
            redisResult = redisResult.stream().filter(Objects::nonNull).toList();
        }
        List<FindByUserIdRespDTO> resultList = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(redisResult)){
            resultList = redisResult.stream()
                    .map(value-> JsonUtils.Parse_Object(String.valueOf(value),FindByUserIdRespDTO.class))
                    .collect(Collectors.toList());
            if(resultList.size() == userId.size()) {
                return Response.success(resultList);
            }
        }
        //get user info from database
        //part of user need to query
        List<Long> userNeedQuery = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(redisResult)){
            Map<Long, FindByUserIdRespDTO> redisResultMap = resultList.stream().collect(Collectors.toMap(FindByUserIdRespDTO::getUserId, p -> p));
            userNeedQuery = userId.stream().filter(id -> Objects.isNull(redisResultMap.get(id))).toList();
        }else{
            //all user need to query
            userNeedQuery = userId;
        }
        //query left user info from database
        List<FindByUserIdRespDTO> leftResultList = null;
        List<UserDO> leftUsers = userDOMapper.selectUsersByIds(userNeedQuery);
        leftUsers = leftUsers.stream().filter(Objects::nonNull).toList();
        //put these users info into redis
        if(CollectionUtil.isNotEmpty(leftUsers)) {
            log.info("=========put left user info into redis");
            leftResultList = leftUsers.stream()
                    .map(user -> FindByUserIdRespDTO.builder()
                            .userId(user.getId())
                            .avatar(user.getAvatar())
                            .nickName(user.getNickname())
                            .introduction(user.getIntroduction())
                            .build())
                    .collect(Collectors.toList());
            List<FindByUserIdRespDTO> finalLeftUserInfo = leftResultList;
            //todo put into redis
            threadPoolTaskExecutor.submit(()->{
                Map<Long, FindByUserIdRespDTO> redisMap = finalLeftUserInfo.stream().collect(Collectors.toMap(FindByUserIdRespDTO::getUserId, p -> p));
                //execute redis pipeline
                redisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        for (Long  userId : redisMap.keySet()) {
                            String redisKey = RedisConstants.buildUserInfoKey(userId);
                            String redisValue = JsonUtils.toJsonString(redisMap.get(userId));
                            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
                            redisTemplate.opsForValue().set(redisKey,redisValue,expireSeconds, TimeUnit.SECONDS);
                        }
                        return null;
                    }
                });
            });
        }
        //merge data
        if(CollUtil.isNotEmpty(leftResultList)) {
            log.info("==========merge data");
            resultList.addAll(leftResultList);
        }
        return Response.success(resultList);
    }

}
