package com.quanxiaoha.xiaohashu.note.biz.controller;

import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.note.biz.model.vo.req.*;
import com.quanxiaoha.xiaohashu.note.biz.model.vo.resp.GetNoteDetailRespVO;
import com.quanxiaoha.xiaohashu.note.biz.service.NoteService;
import com.quanxiaoha.xiaoshu.operationlog.aspect.ApiOperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/note")
public class NoteController {
    @Autowired
    private NoteService noteService;
    @PostMapping("/publish")
    @ApiOperationLog(description = "发布笔记")
    public Response<?> publishNote(@Validated @RequestBody PublishNoteVO publishNoteVO){
        return noteService.publishNote(publishNoteVO);
    }

    @PostMapping("/detail")
    @ApiOperationLog(description = "查询笔记详情")
    public Response<GetNoteDetailRespVO> publishNote(@Validated @RequestBody GetNoteDetailReqVO getNoteDetailReqVO){
        return noteService.getNoteDetail(getNoteDetailReqVO);
    }

    @PutMapping("/update")
    @ApiOperationLog(description = "笔记修改")
    public Response<?> publishNote(@Validated @RequestBody UpdateNoteReqVO updateNoteReqVO){
        return noteService.updateNote(updateNoteReqVO);
    }

    @DeleteMapping("/delete")
    @ApiOperationLog(description = "删除笔记")
    public Response<?> deleteNote(@Validated @RequestBody DeleteNoteReqVO deleteNoteReqVO){
        return noteService.deleteNote(deleteNoteReqVO);
    }
    @PutMapping("/private")
    @ApiOperationLog(description = "设置笔记为隐私")
    public Response<?> setNotePrivate(@Validated @RequestBody SetNotePrivateReqVO setNotePrivateReqVO){
        return noteService.setNotePrivate(setNotePrivateReqVO);
    }

    @PutMapping("/top")
    @ApiOperationLog(description = "设置笔记为置顶")
    public Response<?> setNoteTop(@Validated @RequestBody SetNoteTopReqVO setNoteTopReqVO){
        return noteService.setNoteTop(setNoteTopReqVO);
    }

    @PostMapping("/like")
    @ApiOperationLog(description = "笔记点赞")
    public Response<?> likeNote(@Validated @RequestBody LikeNoteReqVO likeNoteReqVO){
        return noteService.likeNote(likeNoteReqVO);
    }




}
