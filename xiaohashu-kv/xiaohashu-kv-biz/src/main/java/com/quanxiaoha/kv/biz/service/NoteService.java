package com.quanxiaoha.kv.biz.service;

import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.AddNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.DeleteNoteContentDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.FindNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.resp.FindNoteContentRespDTO;

public interface NoteService{
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);
    Response<FindNoteContentRespDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO);
    Response<?> deleteNoteContent(DeleteNoteContentDTO deleteNoteContentDTO);
}
