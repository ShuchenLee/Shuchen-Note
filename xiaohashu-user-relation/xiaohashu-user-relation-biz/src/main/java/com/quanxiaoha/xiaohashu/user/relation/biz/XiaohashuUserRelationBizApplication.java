package com.quanxiaoha.xiaohashu.user.relation.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.quanxiaoha.xiaohashu")
@SpringBootApplication
@MapperScan("com.quanxiaoha.xiaohashu.user.relation.biz.domain.mapper")
public class XiaohashuUserRelationBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaohashuUserRelationBizApplication.class, args);
    }
}
