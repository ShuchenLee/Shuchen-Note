package com.quanxiaoha.xiaohashu.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@MapperScan("com.quanxiaoha.xiaohashu.user.biz.domain.mapper")
@SpringBootApplication
@EnableFeignClients(basePackages = "com.quanxiaoha.xiaohashu")
public class XiaohashuUserApplication {
    public static void main(String[] args) {

        ConfigurableApplicationContext run = SpringApplication.run(XiaohashuUserApplication.class, args);
    }

}
