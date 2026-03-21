package com.quanxiaoha.xiaohashu.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "elasticsearch")
@Component
@Data
public class ESProperties {
    private String address;
}
