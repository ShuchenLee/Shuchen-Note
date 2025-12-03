package com.quanxiaoha.xiaohashu.api;

import com.quanxiaoha.xiaohashu.constants.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface DistributedIdGeneratorFeignApi {
    String PREFIX = "/id";
    @RequestMapping(value = PREFIX+"/segment/get/{key}")
    String getSegmentId(@PathVariable("key") String key);

    @RequestMapping(value = PREFIX+"/snowflake/get/{key}")
    String getSnowflakeId(@PathVariable("key") String key);
}
