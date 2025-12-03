package com.quanxiaoha.xiaohashu.auth.filter;

import cn.hutool.core.util.StrUtil;
import com.quanxiaoha.xiaohashu.common.constant.GlobalConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
public class UserIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader(GlobalConstants.UserId);
        //id不存在
        if (StrUtil.isBlank(userId)) {
            filterChain.doFilter(request,response);
            return ;
        }
        //id存在,放入上下文
        log.info("=====从请求头中获取userId {}",userId);
        UserIdContextHolder.setUserId(userId);
        //转发请求
        try{
            filterChain.doFilter(request,response);
        }finally{
            UserIdContextHolder.removeUserId();
            log.info("======删除ThreadLocal userId {}",userId);
        }
    }
}
