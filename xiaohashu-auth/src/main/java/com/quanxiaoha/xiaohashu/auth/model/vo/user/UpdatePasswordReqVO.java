package com.quanxiaoha.xiaohashu.auth.model.vo.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class UpdatePasswordReqVO {
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
