package com.quanxiaoha.xiaohashu.auth.controller;

import com.quanxiaoha.xiaohashu.context.filter.UserIdContextFilter;
import com.quanxiaoha.xiaoshu.operationlog.aspect.ApiOperationLog;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.auth.model.vo.user.UpdatePasswordReqVO;
import com.quanxiaoha.xiaohashu.auth.model.vo.user.UserLoginVO;
import com.quanxiaoha.xiaohashu.auth.service.AuthrService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class AuthController {
    @Autowired
    private AuthrService authrService;
    @PostMapping("/login")
    @ApiOperationLog(description = "用户登录/注册")
    public Response<String> login(UserLoginVO userLoginVO) {
        return authrService.loginAndRegister(userLoginVO);

    }
    @PutMapping("/password/update")
    @ApiOperationLog(description = "用户密码修改")
    public Response<String> updatePassword(@Validated @RequestBody UpdatePasswordReqVO updatePasswordReqVO){
        return authrService.updatePassword(updatePasswordReqVO);
    }
    @PostMapping("/logout")
    @ApiOperationLog(description="用户登出")
    public Response<String> logout(){
        authrService.loginOut();
        return Response.success();
    }
}
