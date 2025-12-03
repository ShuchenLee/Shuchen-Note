package com.quanxiaoha.xiaohashu.kv.api.dto.api;

import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.kv.api.dto.constant.ApiConstants;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.AddNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.FindNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.resp.FindNoteContentRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface NoteApi {
    String PREFIX = "/kv";
    @PostMapping(value = PREFIX+"/note/content/add")
    Response<?> addNote(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);
    @GetMapping(value = PREFIX+"/note/content/find")
    Response<FindNoteContentRespDTO> findNote(@RequestBody FindNoteContentReqDTO findNoteContentReqDTO);
}
