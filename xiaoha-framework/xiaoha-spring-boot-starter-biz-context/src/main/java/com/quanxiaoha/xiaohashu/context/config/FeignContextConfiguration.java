package com.quanxiaoha.xiaohashu.context.config;

import com.quanxiaoha.xiaohashu.context.Interceptor.FeignRequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class FeignContextConfiguration {
    @Bean
    public FeignRequestInterceptor feignRequestInterceptor(){
        return new FeignRequestInterceptor();
    }
}
