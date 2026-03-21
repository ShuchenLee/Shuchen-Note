package com.quanxiaoha.xiaohashu.search.config;

import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ESRestHighLevelClient {
    @Resource
    private ESProperties esProperties;
    private static final String COLON = ":";
    private static final String HTTP = "http";
    @Bean
    public RestHighLevelClient getClient() {
        String address = esProperties.getAddress();
        String[] addreddArr = address.split(COLON);
        String host = addreddArr[0];
        int port = Integer.parseInt(addreddArr[1]);
        HttpHost httpHost = new HttpHost(host, port, HTTP);
        return new RestHighLevelClient(RestClient.builder(httpHost));
    }
}
