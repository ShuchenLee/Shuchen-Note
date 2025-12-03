package com.quanxiaoha.xiaohashu.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class PhoneValidator implements ConstraintValidator<PhoneNumber,String> {

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        return phone != null && phone.matches("\\d{11}");
    }
}
