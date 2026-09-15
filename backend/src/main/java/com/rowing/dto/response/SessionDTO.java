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
    /** 课次状态：1-有效，0-因支架停用被标掉（持久化，不随支架重新启用自动恢复） */
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 课次当前是否有效：被标停用、超载（人数压过支架当前承重）或支架缺失即为 false */
    private Boolean valid;
    /** 是否因人数超过当前承重而超载 */
    private Boolean overloaded;
    /** 是否因支架停用而不可上（仅对上课日还没到或就是今天的课成立；已过上课日的课保留当时记录） */
    private Boolean bracketDisabled;

    public static SessionDTO from(TrainingSession entity,
                                  String bracketCode, BigDecimal loadCapacity, Integer bracketStatus,
                                  String groupName, String groupCode) {
        boolean overloaded = loadCapacity != null
                && new BigDecimal(entity.getExpectedPersonCount()).compareTo(loadCapacity) > 0;
        // 因支架停用而不可上：停用保存时已被持久标掉（status=0），或查询时支架正处于停用/已删除；
        // 只影响上课日还没到或就是今天的课，已过上课日的课不回溯标停用，留着当时的记录
        boolean markedDisabled = entity.getStatus() != null && entity.getStatus() == 0;
        boolean bracketMissingOrDisabled = bracketStatus == null || bracketStatus != 1;
        boolean bracketDisabled = (markedDisabled || bracketMissingOrDisabled)
                && !entity.getSessionDate().isBefore(LocalDate.now());
        boolean valid = loadCapacity != null && !overloaded && !bracketDisabled;

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
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .valid(valid)
                .overloaded(overloaded)
                .bracketDisabled(bracketDisabled)
                .build();
    }
}
