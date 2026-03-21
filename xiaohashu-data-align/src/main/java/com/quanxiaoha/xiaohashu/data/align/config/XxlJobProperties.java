package com.quanxiaoha.xiaohashu.data.align.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = XxlJobProperties.PREFIX)
@Component
@Data
public class XxlJobProperties {

    public static final String PREFIX = "xxl.job";

    private String adminAddresses;

    private String accessToken;

    private String appName;

    private String ip;

    private int port;

    private String logPath;

    private int logRetentionDays = 30;
}