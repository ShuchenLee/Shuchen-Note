package com.quanxiaoha.xiaohashu.user.biz.controller;

import com.quanxiaoha.xiaohashu.user.api.dto.req.*;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserIdRespDTO;
import com.quanxiaoha.xiaoshu.operationlog.aspect.ApiOperationLog;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.user.biz.vo.UpdateUserReqVO;
import com.quanxiaoha.xiaohashu.user.biz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
@RestController
public class UserContorller {
    @Autowired
    private UserService userService;

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> updateUserInfo(@Validated UpdateUserReqVO updateUserReqVO){
        return userService.updateUserInfo(updateUserReqVO);
    }

    // ===================================== 对其他服务提供的接口 =====================================
    @PostMapping("/register")
    @ApiOperationLog(description = "用户注册")
    public Response<?> registerUser(@Validated @RequestBody RegisterUserReqDTO registerUserReqDTO){
        return userService.registerUser(registerUserReqDTO);
    }

    @PostMapping("/findByPhone")
    @ApiOperationLog(description = "通过手机号查询用户信息")
    public Response<?> findUserByPhone(@Validated @RequestBody FindByUserPhoneReqDTO findUserReqDTO){
        return userService.findByPhone(findUserReqDTO);
    }

    @PutMapping("/password/update")
    @ApiOperationLog(description = "更改用户密码")
    public Response<?> updatePassword(@Validated @RequestBody UpdatePasswordReqDTO updateUserReqDTO){
        return userService.updatePassword(updateUserReqDTO);
    }

    @PostMapping("/findById")
    @ApiOperationLog(description = "通过用户Id查询用户信息")
    public Response<FindByUserIdRespDTO> findById(@Validated @RequestBody FindByUserIdReqDTO findByUserIdReqDTO){
        return userService.findUserById(findByUserIdReqDTO);
    }
    @PostMapping("/findByIds")
    @ApiOperationLog(description = "批量查询用户信息")
    public Response<List<FindByUserIdRespDTO>> findByIds(@Validated @RequestBody FindUsersByIdsReqDTO findUsersByIdsReqDTO){
        return userService.findUsersByIds(findUsersByIdsReqDTO);
    }
}
