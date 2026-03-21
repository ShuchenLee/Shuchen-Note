package com.quanxiaoha.xiaohashu.search.service;

import com.quanxiaoha.xiaohashu.common.response.PageResponse;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchNoteReqVO;
import com.quanxiaoha.xiaohashu.search.model.VO.SearchNoteRespVO;

public interface NoteSearchService {
    public PageResponse<SearchNoteRespVO> searchNote(SearchNoteReqVO searchNoteReqVO);
}
