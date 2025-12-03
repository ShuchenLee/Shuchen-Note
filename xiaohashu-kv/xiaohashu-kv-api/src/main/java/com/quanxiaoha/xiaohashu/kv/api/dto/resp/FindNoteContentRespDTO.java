package com.quanxiaoha.xiaohashu.kv.api.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteContentRespDTO {
    String noteId;
    String content;
}
