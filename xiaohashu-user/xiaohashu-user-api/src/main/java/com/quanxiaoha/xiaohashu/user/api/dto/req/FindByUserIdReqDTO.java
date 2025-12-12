package com.quanxiaoha.xiaohashu.user.api.dto.req;

import com.quanxiaoha.xiaohashu.common.validator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindByUserIdReqDTO {
    /**
     * 手机号
     */
    @NotNull(message = "用户Id不能为空")
    private Long userId;
}
