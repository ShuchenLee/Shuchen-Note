package com.quanxiaoha.xiaohashu.user.api.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUsersByIdsReqDTO {
    /**
     * 手机号
     */
    @NotNull(message = "用户Id集合不能为空")
    @Size(min =1,max = 10,message = "用户Id集合必须大于等于1，小于等于10")
    private List<Long> userIdList;
}
