package com.quanxiaoha.xiaohashu.user.relation.biz.rpc;

import com.quanxiaoha.xiaohashu.common.response.PageResponse;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.user.api.dto.req.FindByUserIdReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.req.FindUsersByIdsReqDTO;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserIdRespDTO;
import com.quanxiaoha.xiaohashu.user.api.userapi.UserFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class UserRpcService {
    @Resource
    private UserFeignApi userFeignApi;

    public FindByUserIdRespDTO findById(Long userId) {
        FindByUserIdReqDTO reqDTO = FindByUserIdReqDTO.builder().userId(userId).build();
        Response<FindByUserIdRespDTO> response = userFeignApi.findUserById(reqDTO);
        if(!response.isSuccess() || Objects.isNull(response.getData())){
            return null;
        }
        return response.getData();
    }

    public List<FindByUserIdRespDTO> findByIds(List<Long> userIds) {
        FindUsersByIdsReqDTO findUsersByIdsReqDTO = FindUsersByIdsReqDTO.builder()
                .userIdList(userIds)
                .build();
        Response<List<FindByUserIdRespDTO>> response = userFeignApi.findUsersByIds(findUsersByIdsReqDTO);
        if(!response.isSuccess() || Objects.isNull(response.getData())){
            return null;
        }
        return response.getData();

    }
}
