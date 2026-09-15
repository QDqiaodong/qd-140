package com.rowing.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionUpdateRequest {

    @NotNull(message = "课次ID不能为空")
    private Long id;

    @NotNull(message = "训练日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;

    @NotNull(message = "停靠支架不能为空")
    private Long bracketId;

    private Long groupId;

    /**
     * 人数必填且不能超过支架当前承重；统一由 Service 抛业务异常，
     * 拦截消息中写清“承重 X 人、本次 Y 人”。
     */
    private Integer expectedPersonCount;

    private String remark;
}
