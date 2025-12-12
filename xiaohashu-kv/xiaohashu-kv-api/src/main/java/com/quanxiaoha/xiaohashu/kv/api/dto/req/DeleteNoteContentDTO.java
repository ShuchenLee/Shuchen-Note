package com.quanxiaoha.xiaohashu.kv.api.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteNoteContentDTO {
    @NotNull(message = "笔记 ID 不能为空")
    private String uuid;
}
