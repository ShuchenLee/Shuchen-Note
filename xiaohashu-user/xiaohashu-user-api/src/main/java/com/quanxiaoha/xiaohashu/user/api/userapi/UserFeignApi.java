package com.quanxiaoha.xiaohashu.user.api.userapi;

import com.quanxiaoha.xiaohashu.user.api.constants.ApiConstants;
import com.quanxiaoha.xiaohashu.user.api.dto.req.FindByUserPhoneReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.req.RegisterUserReqDTO;
import com.quanxiaoha.xiaohashu.common.response.Response;

import com.quanxiaoha.xiaohashu.user.api.dto.req.UpdatePasswordReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserPhoneRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface UserFeignApi {
    String PREFIX = "/user";
    /**
     * 用户注册
     *
     * @param registerUserReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/register")
    Response<Long> registerUser(@RequestBody RegisterUserReqDTO registerUserReqDTO);

    @PostMapping(value = PREFIX + "/findByPhone")
    public Response<FindByUserPhoneRespDTO> findUserByPhone(@RequestBody FindByUserPhoneReqDTO findUserReqDTO);
    @PutMapping(value = PREFIX + "/password/update")
    public Response<?>  updatePassword(@RequestBody UpdatePasswordReqDTO updateUserReqDTO);
}
