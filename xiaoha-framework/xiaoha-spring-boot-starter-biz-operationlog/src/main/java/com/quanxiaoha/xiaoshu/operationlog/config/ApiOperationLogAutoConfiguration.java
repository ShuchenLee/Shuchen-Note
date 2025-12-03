package com.quanxiaoha.xiaoshu.operationlog.config;

import com.quanxiaoha.xiaoshu.operationlog.aspect.ApiOperationLogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
@AutoConfiguration
public class ApiOperationLogAutoConfiguration {
    @Bean
    public ApiOperationLogAspect apiOperationLogAspect() {
        System.out.println("切面类初始化");
        return new ApiOperationLogAspect();
    }
}
