package com.quanxiaoha.xiaohashu.auth.filter;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.quanxiaoha.xiaohashu.common.constant.GlobalConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserIdContextHolder {
    private static ThreadLocal<Map<String,Object>> LOGIN_USER_CONTEXT_THREAD_LOCAL = TransmittableThreadLocal.withInitial(HashMap::new);
    public static void setUserId(Object value) {
        LOGIN_USER_CONTEXT_THREAD_LOCAL.get().put(GlobalConstants.UserId, value);
    }
    public static Object getUserId() {
        Object value = LOGIN_USER_CONTEXT_THREAD_LOCAL.get().get(GlobalConstants.UserId);
        if (Objects.isNull(value)) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }
    public static void removeUserId() {
        LOGIN_USER_CONTEXT_THREAD_LOCAL.remove();
    }
}
