package com.rowing.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindingRequest {

    @NotNull(message = "支架ID不能为空")
    private Long bracketId;

    @NotNull(message = "组别ID不能为空")
    private Long groupId;

    private String operator;

    private String reason;
}