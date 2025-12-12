package com.quanxiaoha.xiaohashu.common.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class PageResponse<T> extends Response<List<T>>{
    //current pageNo
    private long pageNo;
    //total data num
    private long totalCount;
    //pageSize
    private long pageSize;
    //total page
    private long totalPage;
    public static <T> PageResponse<T> success(List<T> data,long pageNo ,long totalCount){
        PageResponse<T> pageResponse = new PageResponse<T>();
        pageResponse.setSuccess(true);
        pageResponse.setPageNo(pageNo);
        pageResponse.setTotalCount(totalCount);
        pageResponse.setData(data);
        long pageSize = 10L;
        pageResponse.setPageSize(pageSize);
        pageResponse.setTotalPage((totalCount + pageSize - 1) / pageSize);
        return  pageResponse;

    }
    public static <T> PageResponse<T> success(List<T> data,long pageNo ,long totalCount,long pageSize){
        PageResponse<T> pageResponse = new PageResponse<T>();
        pageResponse.setSuccess(true);
        pageResponse.setPageNo(pageNo);
        pageResponse.setTotalCount(totalCount);
        pageResponse.setData(data);
        pageResponse.setPageSize(pageSize);
        pageResponse.setTotalPage(pageSize == 0? 0:(totalCount + pageSize - 1) / pageSize);
        return  pageResponse;
    }
    public long getToalPage(){
        return pageSize == 0? 0:(totalCount + pageSize - 1) / pageSize;
    }
}
