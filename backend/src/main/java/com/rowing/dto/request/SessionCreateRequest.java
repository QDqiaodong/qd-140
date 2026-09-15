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
public class SessionCreateRequest {

    @NotNull(message = "训练日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sessionDate;

    @NotNull(message = "停靠支架不能为空")
    private Long bracketId;

    private Long groupId;

    /**
     * 人数必填且不能超过支架当前承重；不在这里用 @NotNull/@Min，
     * 以便统一由 Service 抛出带“承重 X 人、本次 Y 人”明细的业务异常。
     */
    private Integer expectedPersonCount;

    private String remark;
}
