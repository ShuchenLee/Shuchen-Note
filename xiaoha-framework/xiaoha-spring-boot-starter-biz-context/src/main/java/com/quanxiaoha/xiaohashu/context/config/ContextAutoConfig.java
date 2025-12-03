package com.quanxiaoha.xiaohashu.context.config;

import com.quanxiaoha.xiaohashu.context.filter.UserIdContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ContextAutoConfig {
    @Bean
    public FilterRegistrationBean<UserIdContextFilter> filterRegistrationBean() {
        UserIdContextFilter filter = new UserIdContextFilter();
        FilterRegistrationBean<UserIdContextFilter> bean = new FilterRegistrationBean<>(filter);
        return bean;
    }
}
