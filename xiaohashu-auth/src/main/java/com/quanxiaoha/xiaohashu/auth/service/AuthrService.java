package com.quanxiaoha.xiaohashu.auth.service;

import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.auth.model.vo.user.UpdatePasswordReqVO;
import com.quanxiaoha.xiaohashu.auth.model.vo.user.UserLoginVO;

public interface AuthrService {
    Response<String> loginAndRegister(UserLoginVO userLoginVO);
    Response<String> loginOut();

    Response<String> updatePassword(UpdatePasswordReqVO updatePasswordVO);
}
