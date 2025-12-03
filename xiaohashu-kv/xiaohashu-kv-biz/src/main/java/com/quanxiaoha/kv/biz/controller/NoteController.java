package com.quanxiaoha.kv.biz.controller;

import com.quanxiaoha.kv.biz.service.NoteService;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.AddNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.FindNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.resp.FindNoteContentRespDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping ("/kv")
@RestController
public class NoteController {
    @Autowired
    private NoteService noteService;

    @PostMapping("/note/content/add")
    public Response<?> addNote(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO) {
        return noteService.addNoteContent(addNoteContentReqDTO);
    }
    @GetMapping("note/content/find")
    public Response<FindNoteContentRespDTO> findNoteContent(@Validated @RequestBody FindNoteContentReqDTO findNoteContentReqDTO) {
        return noteService.findNoteContent(findNoteContentReqDTO);
    }
}
