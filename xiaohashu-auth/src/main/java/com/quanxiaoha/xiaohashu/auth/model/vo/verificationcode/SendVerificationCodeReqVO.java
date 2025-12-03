package com.quanxiaoha.xiaohashu.auth.model.vo.verificationcode;

import com.quanxiaoha.xiaohashu.common.validator.PhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendVerificationCodeReqVO {
    @NotBlank(message = "电话后不能为空")
    @PhoneNumber
    private String phone;
}
