package com.quanxiaoha.xiaohashu.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aliyun")
@Component
@Data
public class ALiYunAccessProperty {
    private String accessKeyId;
    private String accessKeySecret;
}
