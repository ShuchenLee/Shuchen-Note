package com.quanxiaoha.xiaohashu.user.biz.rpc;


import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.oss.api.fileapi.FileFeignApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
@Component
public class OssRpcService {
    @Autowired
    private FileFeignApi fileFeignApi;

    public String uploadFile(MultipartFile file) {
        // 调用对象存储服务上传文件
        Response<?> response = fileFeignApi.uploadFile(file);

        if (!response.isSuccess()) {
            return null;
        }

        // 返回图片访问链接
        return (String) response.getData();
    }

}
