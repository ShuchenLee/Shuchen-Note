package com.quanxiaoha.xiaohashu.note.biz.rpc;

import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.user.api.dto.req.FindByUserIdReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserIdRespDTO;
import com.quanxiaoha.xiaohashu.user.api.userapi.UserFeignApi;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserRpcService {
    @Autowired
    @Resource
    private UserFeignApi userFeignApi;
    public FindByUserIdRespDTO findUserById(Long userId){
        //construct DTO object
        FindByUserIdReqDTO findByUserIdReqDTO = FindByUserIdReqDTO.builder().userId(userId).build();
        //get response from user service
        Response<FindByUserIdRespDTO> result = userFeignApi.findUserById(findByUserIdReqDTO);
        if(Objects.isNull(result) || !result.isSuccess()){
            return null;
        }
        return result.getData();

    }

}
