package com.quanxiaoha.xiaohashu.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import com.quanxiaoha.xiaohashu.common.constant.GlobalConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Slf4j
@Component
public class AddUserIdFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("正在网关进行信息传递");
        // 用户 ID
        Long userId = null;
        try {
            // 获取当前登录用户的 ID
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 若没有登录，则直接放行
            return chain.filter(exchange);
        }
        log.info("userId: {}", userId);
        //将userId添加到上下文
        Long finalUserId = userId;
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(GlobalConstants.UserId, String.valueOf(finalUserId)))
                .build();


        return chain.filter(newExchange);
    }
}
