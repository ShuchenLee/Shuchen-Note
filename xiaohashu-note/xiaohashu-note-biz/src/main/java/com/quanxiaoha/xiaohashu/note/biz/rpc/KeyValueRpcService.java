package com.quanxiaoha.xiaohashu.note.biz.rpc;

import com.quanxiaoha.xiaohashu.common.response.Response;
import com.quanxiaoha.xiaohashu.kv.api.dto.api.KeyValueFeignApi;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.AddNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.DeleteNoteContentDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.req.FindNoteContentReqDTO;
import com.quanxiaoha.xiaohashu.kv.api.dto.resp.FindNoteContentRespDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class KeyValueRpcService {
    @Autowired
    private KeyValueFeignApi keyValueFeignApi;

    public boolean saveNoteContent(String uuid, String content){
        AddNoteContentReqDTO addNoteContentReqDTO = new AddNoteContentReqDTO();
        addNoteContentReqDTO.setContent(content);
        addNoteContentReqDTO.setUuid(uuid);

        Response<?> response = keyValueFeignApi.addNote(addNoteContentReqDTO);
        if (Objects.isNull(response) || !response.isSuccess()) {
            return false;
        }

        return true;

    }

    public boolean deleteNoteContent(String uuid){
        DeleteNoteContentDTO deleteNoteContentDTO = new DeleteNoteContentDTO();
        deleteNoteContentDTO.setUuid(uuid);
        Response<?> response = keyValueFeignApi.deleteNote(deleteNoteContentDTO);
        if (Objects.isNull(response) || !response.isSuccess()) {
            return false;
        }
        return true;
    }
    public String getNodeContent(String uuid){
        FindNoteContentReqDTO findNoteContentReqDTO = FindNoteContentReqDTO.builder().uuid(uuid).build();
        Response<FindNoteContentRespDTO> response = keyValueFeignApi.findNote(findNoteContentReqDTO);
        if (Objects.isNull(response) || !response.isSuccess() || Objects.isNull(response.getData())) {
            return null;
        }
        return response.getData().getContent();
    }

}
