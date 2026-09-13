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
public class BracketUpdateRequest {

    @NotNull(message = "支架ID不能为空")
    private Long id;

    @Size(max = 50, message = "支架编号长度不能超过50")
    private String bracketCode;

    @DecimalMin(value = "0.01", message = "承重必须大于0")
    private BigDecimal loadCapacity;

    @Min(value = 1, message = "最小距离必须大于0")
    private Integer minDistance;

    @Min(value = 1, message = "最大距离必须大于0")
    private Integer maxDistance;

    private Integer status;

    private String remark;
}