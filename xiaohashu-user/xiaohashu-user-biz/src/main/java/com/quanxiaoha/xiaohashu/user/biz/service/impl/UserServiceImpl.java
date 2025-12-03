package com.quanxiaoha.xiaohashu.user.biz.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.quanxiaoha.xiaohashu.common.enums.DeletedEnum;
import com.quanxiaoha.xiaohashu.common.enums.StatusEnum;
import com.quanxiaoha.xiaohashu.common.exception.BizException;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.common.util.ParamUtils;
import com.quanxiaoha.xiaohashu.context.holder.ContextHolder;
import com.quanxiaoha.xiaohashu.oss.api.fileapi.FileFeignApi;
import com.quanxiaoha.xiaohashu.user.api.dto.req.FindByUserPhoneReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.req.RegisterUserReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.req.UpdatePasswordReqDTO;
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
import com.quanxiaoha.xiaohashu.user.biz.vo.UpdateUserReqVO;
import com.quanxiaoha.xiaohashu.user.biz.rpc.OssRpcService;
import com.quanxiaoha.xiaohashu.user.biz.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private RoleDOMapper roleDOMapper;
    @Autowired
    private UserRoleDOMapper userRoleDOMapper;
    @Autowired
    private OssRpcService ossRpcService;
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private RedisTemplate redisTemplate;
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
        Long xiaohashuId = redisTemplate.opsForValue().increment(RedisConstants.XIAOHASHU_ID_GENERATOR_KEY);
        UserDO userDO = UserDO.builder()
                .phone(phone)
                .xiaohashuId(String.valueOf(xiaohashuId))
                .nickname("小红薯" + xiaohashuId)
                .status(StatusEnum.ENABLE.getValue())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        userDOMapper.insert(userDO);
        Long userId = userDO.getId();
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
}
