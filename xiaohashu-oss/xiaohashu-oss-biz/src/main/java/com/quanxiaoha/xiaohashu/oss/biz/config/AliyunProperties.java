package com.quanxiaoha.xiaohashu.oss.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "storage.aliyun")
@Component
@Data
public class AliyunProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
}
