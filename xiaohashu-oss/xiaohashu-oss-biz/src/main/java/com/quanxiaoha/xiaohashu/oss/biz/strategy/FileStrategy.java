package com.quanxiaoha.xiaohashu.oss.biz.strategy;

import org.springframework.web.multipart.MultipartFile;

public interface FileStrategy {
    /**
     * upload file
     * @param file
     * @return
     */
    String upload(MultipartFile file,String BucketName);
}
