package com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowListReqVO {
    @NotNull(message = "用户id不能为空")
    private Long userId;
    @NotNull(message = "查询页码不能为空")
    private long pageNo;
}
