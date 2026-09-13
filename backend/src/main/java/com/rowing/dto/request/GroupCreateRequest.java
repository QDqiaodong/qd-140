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
public class GroupCreateRequest {

    @NotBlank(message = "组别名称不能为空")
    @Size(max = 100, message = "组别名称长度不能超过100")
    private String groupName;

    @NotBlank(message = "组别编码不能为空")
    @Size(max = 50, message = "组别编码长度不能超过50")
    private String groupCode;

    @NotNull(message = "竞速距离不能为空")
    @Min(value = 1, message = "竞速距离必须大于0")
    private Integer racingDistance;

    private String description;
}