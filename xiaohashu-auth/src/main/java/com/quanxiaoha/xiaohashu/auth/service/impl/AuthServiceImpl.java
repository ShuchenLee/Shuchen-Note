package com.quanxiaoha.xiaohashu.auth.service.impl;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.quanxiaoha.xiaohashu.auth.rpc.UserRpcService;
import com.quanxiaoha.xiaohashu.context.holder.ContextHolder;
import com.quanxiaoha.xiaohashu.common.exception.BizException;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.auth.constant.RedisConstants;
import com.quanxiaoha.xiaohashu.auth.enums.LoginTypeEnum;
import com.quanxiaoha.xiaohashu.auth.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.auth.model.vo.user.UpdatePasswordReqVO;
import com.quanxiaoha.xiaohashu.auth.model.vo.user.UserLoginVO;
import com.quanxiaoha.xiaohashu.auth.service.AuthrService;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserPhoneRespDTO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Objects;

@Service
public class AuthServiceImpl implements AuthrService {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserRpcService userRpcService;

    @Override
    public Response<String> loginAndRegister(UserLoginVO userLoginVO) {
        //get login type and phone from request
        Integer type = userLoginVO.getType();
        String phone = userLoginVO.getPhone();

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);
        Long userId = null;
        // 登录类型错误
        if (Objects.isNull(loginTypeEnum)) {
            throw new BizException(ResponseCodeEnum.LOGIN_TYPE_ERROR);
        }
        switch (loginTypeEnum){
            //resolve login request with code
            case VERIFICATION_CODE -> {
                String requestCode = userLoginVO.getCode();
                Preconditions.checkArgument(StringUtils.isNotBlank(requestCode), "验证码不能为空");
                String redisKey = RedisConstants.buildVerificationCodeKey(phone);
                String verificationCode = (String) redisTemplate.opsForValue().get(redisKey);

                if(!StringUtils.equals(requestCode,verificationCode)){
                    throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }
                //user rpc service
                Long userIdTmp = userRpcService.registerUser(phone);
                // 若调用用户服务，返回的用户 ID 为空，则提示登录失败
                if (Objects.isNull(userIdTmp)) {
                    throw new BizException(ResponseCodeEnum.LOGIN_FAIL);
                }
                userId = userIdTmp;

            }
            case PASSWORD -> {
                //get password from request
                String password = userLoginVO.getPassword();
                //get UserDO according to phone
                FindByUserPhoneRespDTO findByUserPhoneRespDTO = userRpcService.findUserByPhone(phone);
                if(Objects.isNull(findByUserPhoneRespDTO)){
                    throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
                }
                //get user encoded password
                String encodePassword = findByUserPhoneRespDTO.getPassword();
                //verify password
                boolean flag = passwordEncoder.matches(password,encodePassword);
                if(!flag){
                    throw new BizException(ResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
                }
                userId = findByUserPhoneRespDTO.getId();
            }

        }
        // SaToken 登录用户, 入参为用户 ID
        StpUtil.login(userId);

        // 获取 Token 令牌
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 返回 Token 令牌
        return Response.success(tokenInfo.tokenValue);
    }

    @Override
    public Response<String> loginOut() {
        Long userId = (Long) ContextHolder.getUserId();
        StpUtil.logout(userId);
        return Response.success();
    }

    @Override
    public Response<String> updatePassword(UpdatePasswordReqVO updatePasswordVO) {
        //get new password
        String newPassword = updatePasswordVO.getNewPassword();
        //encode new password
        String encodedPassword = passwordEncoder.encode(newPassword);
        userRpcService.updatePassword(encodedPassword);

        return Response.success();
    }


}
