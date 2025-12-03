package com.quanxiaoha.xiaohashu.oss.biz.strategy.impl;

import com.quanxiaoha.xiaohashu.oss.biz.config.MinioProperties;
import com.quanxiaoha.xiaohashu.oss.biz.strategy.FileStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
public class MinioFileStrategy implements FileStrategy {
    @Resource
    private MinioProperties minioProperties;
    @Resource
    private MinioClient minioClient;
    @Override
    @SneakyThrows
    public String upload(MultipartFile file, String bucketName) {
        log.info("=============upload file to minio");
        //if file is null
        if(file == null){
            throw new RuntimeException("file is null");
        }
        //generate file name
        //get original name
        String originalFilename = file.getOriginalFilename();
        //get file suffix
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString().replace("-",".") + suffix;
        log.info("=============upload file name {}",newFileName);
        //upload file
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(newFileName)
                        .stream(file.getInputStream(), file.getSize(),-1)
                        .contentType(file.getContentType())
                        .build()
        );
        //return file url
        String url = String.format("%s/%s/%s", minioProperties.getEndpoint(), bucketName, newFileName);
        log.info("===========>= 上传文件至 Minio 成功，访问路径: {}", url);
        return url;
    }
}
