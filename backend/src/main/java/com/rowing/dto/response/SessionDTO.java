package com.rowing.dto.response;

import com.rowing.entity.TrainingSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {

    private Long id;
    private LocalDate sessionDate;
    private Long bracketId;
    private String bracketCode;
    /** 支架当前承重，超载判定始终以该值为准（支架改承重后实时反映） */
    private BigDecimal loadCapacity;
    private Integer bracketStatus;
    private Long groupId;
    private String groupName;
    private String groupCode;
    private Integer expectedPersonCount;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 课次当前是否有效：人数压过支架当前承重（或支架缺失/禁用）即为 false */
    private Boolean valid;
    /** 是否因人数超过当前承重而超载 */
    private Boolean overloaded;

    public static SessionDTO from(TrainingSession entity,
                                  String bracketCode, BigDecimal loadCapacity, Integer bracketStatus,
                                  String groupName, String groupCode) {
        boolean overloaded = loadCapacity != null
                && new BigDecimal(entity.getExpectedPersonCount()).compareTo(loadCapacity) > 0;
        boolean valid = loadCapacity != null && bracketStatus != null && bracketStatus == 1 && !overloaded;

        return SessionDTO.builder()
                .id(entity.getId())
                .sessionDate(entity.getSessionDate())
                .bracketId(entity.getBracketId())
                .bracketCode(bracketCode)
                .loadCapacity(loadCapacity)
                .bracketStatus(bracketStatus)
                .groupId(entity.getGroupId())
                .groupName(groupName)
                .groupCode(groupCode)
                .expectedPersonCount(entity.getExpectedPersonCount())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .valid(valid)
                .overloaded(overloaded)
                .build();
    }
}
