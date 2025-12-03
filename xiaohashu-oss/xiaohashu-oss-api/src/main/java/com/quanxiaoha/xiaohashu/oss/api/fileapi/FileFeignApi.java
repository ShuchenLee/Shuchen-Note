package com.quanxiaoha.xiaohashu.oss.api.fileapi;

import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.oss.api.config.FeignConfig;
import com.quanxiaoha.xiaohashu.oss.api.constants.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = ApiConstants.SERVICE_NAME,configuration = FeignConfig.class)
public interface FileFeignApi {
    String PREFIX = "/file";

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping(value = PREFIX + "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<?> uploadFile(@RequestPart(value = "file") MultipartFile file);

}
