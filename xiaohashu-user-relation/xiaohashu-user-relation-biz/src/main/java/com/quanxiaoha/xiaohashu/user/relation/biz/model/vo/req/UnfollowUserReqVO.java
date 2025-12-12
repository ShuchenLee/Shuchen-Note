package com.quanxiaoha.xiaohashu.user.relation.biz.model.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnfollowUserReqVO {
    @NotNull(message = "被取消关注用户 ID 不能为空")
    private Long unfollowUserId;
}
