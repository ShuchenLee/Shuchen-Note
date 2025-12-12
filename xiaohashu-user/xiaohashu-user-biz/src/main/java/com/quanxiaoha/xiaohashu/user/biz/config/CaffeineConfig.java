package com.quanxiaoha.xiaohashu.user.biz.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.quanxiaoha.xiaohashu.user.api.dto.resp.FindByUserIdRespDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {
    @Bean(value = "caffeineCache")
    public Cache<Long, FindByUserIdRespDTO> caffeineCache(){
        final Cache<Long, FindByUserIdRespDTO> cache = Caffeine.newBuilder()
                .initialCapacity(10000) // 设置初始容量为 10000 个条目
                .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
                .expireAfterWrite(1, TimeUnit.HOURS) // 设置缓存条目在写入后 1 小时过期
                .build();
        return cache;
    }

}
