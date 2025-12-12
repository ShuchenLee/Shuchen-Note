package com.quanxiaoha.xiaohashu.note.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.quanxiaoha.xiaohashu")
@SpringBootApplication
@MapperScan("com.quanxiaoha.xiaohashu.note.biz.domain.mapper")
public class XiaohashuNoteBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaohashuNoteBizApplication.class,args);
    }
}
