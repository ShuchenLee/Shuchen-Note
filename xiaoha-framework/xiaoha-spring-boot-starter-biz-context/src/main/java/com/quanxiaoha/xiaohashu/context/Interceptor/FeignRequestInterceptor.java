package com.quanxiaoha.xiaohashu.context.Interceptor;

import com.quanxiaoha.xiaohashu.common.constant.GlobalConstants;
import com.quanxiaoha.xiaohashu.context.holder.ContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Long userId = (Long)ContextHolder.getUserId();
        if(userId!=null){
            requestTemplate.header(GlobalConstants.UserId,String.valueOf(userId));
            log.info("========feign 请求头添加userid {}",userId);
        }

    }
}
