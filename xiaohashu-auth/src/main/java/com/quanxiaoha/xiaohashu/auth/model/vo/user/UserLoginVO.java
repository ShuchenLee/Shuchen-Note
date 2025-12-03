package com.quanxiaoha.xiaohashu.auth.model.vo.user;

import com.quanxiaoha.xiaohashu.common.validator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserLoginVO {
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @PhoneNumber
    private String phone;

    /**
     * 验证码
     */
    private String code;

    /**
     * 密码
     */
    private String password;

    /**
     * 登录类型：手机号验证码，或者是账号密码
     */
    @NotNull(message = "登录类型不能为空")
    private Integer type;
}
