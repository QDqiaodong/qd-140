package com.rowing.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BracketCreateRequest {

    @NotBlank(message = "支架编号不能为空")
    @Size(max = 50, message = "支架编号长度不能超过50")
    private String bracketCode;

    @NotNull(message = "承重不能为空")
    @DecimalMin(value = "0.01", message = "承重必须大于0")
    private BigDecimal loadCapacity;

    @NotNull(message = "适配最小距离不能为空")
    @Min(value = 1, message = "最小距离必须大于0")
    private Integer minDistance;

    @NotNull(message = "适配最大距离不能为空")
    @Min(value = 1, message = "最大距离必须大于0")
    private Integer maxDistance;

    private String remark;
}