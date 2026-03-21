package com.quanxiaoha.xiaohashu.search.model.VO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SearchUserReqVO {
    @NotBlank(message = "keyword can not be null")
    private String keyword;
    @Min(value = 1,message = "page no can not be smaller than 1")
    private Integer pageNo;
}
