package com.quanxiaoha.xiaoshu.user.xiaohashu.auth;

import cn.hutool.core.util.IdUtil;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanxiaoha.xiaohashu.auth.constant.RedisConstants;
import com.quanxiaoha.xiaohashu.auth.domain.mapper.RoleDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

@SpringBootTest
@Slf4j
class XiaohashuAuthApplicationTests {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 测试插入数据
     */
    @Test
    void testInsert() throws JsonProcessingException {
        long id = IdUtil.getSnowflakeNextId();
        System.out.println(id);
    }

    /**
     * set key value
     */
    @Test
    void testSetKeyValue() throws JsonProcessingException {
        Long loginId = 4L;
        log.info("## 获取用户角色列表, loginId: {}", loginId);

        // 构建 用户-角色 Redis Key
        String userRolesKey = RedisConstants.buildUserRoleKey(loginId);

        // 根据用户 ID ，从 Redis 中获取该用户的角色集合
        String useRolesValue = redisTemplate.opsForValue().get(userRolesKey);
        log.info("key: {}", userRolesKey);
        log.info("roles: {}", useRolesValue);
        // 将 JSON 字符串转换为 List<String> 集合
        String[] userRoleKeys = useRolesValue.split(",");
        List<String> result = Lists.newArrayList();
        for (String userRoleKey : userRoleKeys) {
            result.add(userRoleKey);
        }
        log.info("result: {}", result);

    }
    /**
     * 测试自定义的线程池
     */
    @Test
    void testTaskThreadPool(){
        threadPoolTaskExecutor.execute(()->{
            log.info("你好");
        });
    }

}

