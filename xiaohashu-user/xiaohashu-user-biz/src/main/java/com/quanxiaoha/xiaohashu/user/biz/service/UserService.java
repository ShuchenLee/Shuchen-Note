package com.quanxiaoha.xiaohashu.user.biz.service;

import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.user.api.dto.req.*;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserIdRespDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserPhoneRespDTO;
import com.quanxiaoha.xiaohashu.user.biz.vo.UpdateUserReqVO;

import java.util.List;

public interface UserService {
    Response<?> updateUserInfo(UpdateUserReqVO updateUserReqVO);
    Response<Long> registerUser(RegisterUserReqDTO registerUserReqDTO);
    public Response<FindByUserPhoneRespDTO> findByPhone(FindByUserPhoneReqDTO findUserReqDTO);
    Response<?> updatePassword(UpdatePasswordReqDTO updatePasswordReqDTO);

    Response<FindByUserIdRespDTO> findUserById(FindByUserIdReqDTO findByUserIdReqDTO);

    Response<List<FindByUserIdRespDTO>> findUsersByIds(FindUsersByIdsReqDTO findUsersByIdsReqDTO);

}
