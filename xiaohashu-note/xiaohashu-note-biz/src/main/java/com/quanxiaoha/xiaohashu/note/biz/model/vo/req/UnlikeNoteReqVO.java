package com.quanxiaoha.xiaohashu.note.biz.model.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnlikeNoteReqVO {
    @NotNull(message = "noteId can't be null")
    private Long noteId;
}
