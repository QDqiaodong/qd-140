package com.rowing.dto.response;

import com.rowing.entity.BindingChangeLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeLogDTO {

    private Long id;
    private Long bracketId;
    private String bracketCode;
    private Long groupId;
    private String groupName;
    private String changeType;
    private Integer previousDistance;
    private Integer newDistance;
    private String changeReason;
    private String operator;
    private LocalDateTime changedAt;

    public static ChangeLogDTO fromEntity(BindingChangeLog entity) {
        return ChangeLogDTO.builder()
                .id(entity.getId())
                .bracketId(entity.getBracketId())
                .bracketCode(entity.getBracketCode())
                .groupId(entity.getGroupId())
                .groupName(entity.getGroupName())
                .changeType(entity.getChangeType())
                .previousDistance(entity.getPreviousDistance())
                .newDistance(entity.getNewDistance())
                .changeReason(entity.getChangeReason())
                .operator(entity.getOperator())
                .changedAt(entity.getChangedAt())
                .build();
    }
}