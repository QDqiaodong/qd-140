package com.rowing.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdateRequest {

    @NotNull(message = "组别ID不能为空")
    private Long id;

    @Size(max = 100, message = "组别名称长度不能超过100")
    private String groupName;

    @Size(max = 50, message = "组别编码长度不能超过50")
    private String groupCode;

    @Min(value = 1, message = "竞速距离必须大于0")
    private Integer racingDistance;

    private String description;
}