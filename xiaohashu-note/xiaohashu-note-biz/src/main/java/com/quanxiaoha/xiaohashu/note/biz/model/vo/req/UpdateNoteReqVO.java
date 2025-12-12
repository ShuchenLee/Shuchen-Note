package com.quanxiaoha.xiaohashu.note.biz.model.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateNoteReqVO {
    @NotNull(message = "笔记类型不能为空")
    private Integer type;
    @NotNull(message = "笔记Id不能为空")
    private Long noteId;

    private String videoUri;
    private List<String> imgUris;
    private String title;
    private String content;
    private Long topicId;
}
