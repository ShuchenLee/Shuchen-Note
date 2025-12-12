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
public class FansListReqVO {
    @NotNull(message = "用户id不能为空")
    private Long userId;
    @NotNull(message = "查询页码不能为空")
    private long pageNo;
}
