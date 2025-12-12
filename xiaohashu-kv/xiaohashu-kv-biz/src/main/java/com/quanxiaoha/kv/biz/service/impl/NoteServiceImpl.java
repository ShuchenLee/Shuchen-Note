package com.quanxiaoha.kv.biz.service.impl;

import com.quanxiaoha.kv.biz.constants.ResponseCodeEnum;
import com.quanxiaoha.kv.biz.domain.dataobject.NoteContentDO;
import com.quanxiaoha.kv.biz.domain.respsitory.NoteContentRepository;
import com.quanxiaoha.kv.biz.service.NoteService;
import com.quanxiaoha.xiaohashu.common.exception.BizException;
import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.AddNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.DeleteNoteContentDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.FindNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.resp.FindNoteContentRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class NoteServiceImpl implements NoteService {
    @Resource
    private NoteContentRepository noteContentRepository;
    @Override
    public Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO) {
        //get note id
        String noteId = addNoteContentReqDTO.getUuid();
        //get note content
        String content = addNoteContentReqDTO.getContent();
        //construct NoteDO
        NoteContentDO noteContentDO = NoteContentDO.builder()
                        .id(UUID.fromString(noteId))
                        .content(content)
                .build();
        noteContentRepository.save(noteContentDO);
        return Response.success();
    }

    @Override
    public Response<FindNoteContentRespDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO) {
        //get noteId
        String uuid = findNoteContentReqDTO.getUuid();
        //select note
        Optional<NoteContentDO> option = noteContentRepository.findById(UUID.fromString(uuid));
        if(!option.isPresent()){
            throw new BizException(ResponseCodeEnum.NOTE_NOT_EXIST);
        }
        //construct result
        FindNoteContentRespDTO result = FindNoteContentRespDTO.builder()
                .uuid(option.get().getId().toString())
                .content(option.get().getContent())
                .build();
        return Response.success(result);
    }

    @Override
    public Response<?> deleteNoteContent(DeleteNoteContentDTO deleteNoteContentDTO) {
        //get noteId
        String noteId = deleteNoteContentDTO.getUuid();
        //delete note
        noteContentRepository.deleteById(UUID.fromString(noteId));
        return Response.success();
    }
}
