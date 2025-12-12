package com.quanxiaoha.xiaohashu.note.biz.service;

import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.note.biz.model.vo.req.*;
import com.quanxiaoha.xiaohashu.note.biz.model.vo.resp.GetNoteDetailRespVO;

public interface NoteService {
    Response<?> publishNote(PublishNoteVO publishNoteVO);

    Response<GetNoteDetailRespVO> getNoteDetail(GetNoteDetailReqVO getNoteDetailReqVO);

    Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO);
    public void deleteCache(Long noteId);
    Response<?> deleteNote(DeleteNoteReqVO deleteNoteReqVO);
    Response<?> setNotePrivate(SetNotePrivateReqVO setNotePrivateReqVO);
    Response<?> setNoteTop(SetNoteTopReqVO setNoteTopReqVO);

    Response<?> likeNote(LikeNoteReqVO likeNoteReqVO);

}
