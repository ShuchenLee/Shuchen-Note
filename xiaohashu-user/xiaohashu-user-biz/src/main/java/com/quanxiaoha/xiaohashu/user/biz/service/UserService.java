package com.quanxiaoha.xiaohashu.user.biz.service;

import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.user.api.dto.req.FindByUserPhoneReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.req.RegisterUserReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.req.UpdatePasswordReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserPhoneRespDTO;
import com.quanxiaoha.xiaohashu.user.biz.vo.UpdateUserReqVO;

public interface UserService {
    Response<?> updateUserInfo(UpdateUserReqVO updateUserReqVO);
    Response<Long> registerUser(RegisterUserReqDTO registerUserReqDTO);
    public Response<FindByUserPhoneRespDTO> findByPhone(FindByUserPhoneReqDTO findUserReqDTO);
    Response<?> updatePassword(UpdatePasswordReqDTO updatePasswordReqDTO);
}
