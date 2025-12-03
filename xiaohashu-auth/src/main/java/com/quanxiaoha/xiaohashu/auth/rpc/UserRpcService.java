package com.quanxiaoha.xiaohashu.auth.rpc;

import com.quanxiaoha.xiaohashu.auth.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.common.exception.BizException;
import com.quanxiaoha.xiaohashu.user.api.dto.req.FindByUserPhoneReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.req.RegisterUserReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.req.UpdatePasswordReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserPhoneRespDTO;
import com.quanxiaoha.xiaohashu.user.api.userapi.UserFeignApi;
import com.quanxiaoha.xiaohashu.common.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 用户注册
     *
     * @param phone
     * @return
     */
    public Long registerUser(String phone) {
        RegisterUserReqDTO registerUserReqDTO = new RegisterUserReqDTO();
        registerUserReqDTO.setPhone(phone);

        Response<Long> response = userFeignApi.registerUser(registerUserReqDTO);

        if (!response.isSuccess()) {
            return null;
        }

        return response.getData();
    }

    public FindByUserPhoneRespDTO findUserByPhone(String phone) {
        FindByUserPhoneReqDTO findByUserPhoneReqDTO = new FindByUserPhoneReqDTO();
        findByUserPhoneReqDTO.setPhone(phone);

        Response<FindByUserPhoneRespDTO> response = userFeignApi.findUserByPhone(findByUserPhoneReqDTO);
        if (!response.isSuccess()) {
            return null;
        }

        return response.getData();
    }
    public void updatePassword(String encodePassword) {
        UpdatePasswordReqDTO updateUserPasswordReqDTO = new UpdatePasswordReqDTO();
        updateUserPasswordReqDTO.setNewPassword(encodePassword);
        userFeignApi.updatePassword(updateUserPasswordReqDTO);
    }

}

