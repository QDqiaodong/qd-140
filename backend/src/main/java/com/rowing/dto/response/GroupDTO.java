package com.rowing.dto.response;

import com.rowing.entity.RowingGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {

    private Long id;
    private String groupName;
    private String groupCode;
    private Integer racingDistance;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GroupDTO fromEntity(RowingGroup entity) {
        return GroupDTO.builder()
                .id(entity.getId())
                .groupName(entity.getGroupName())
                .groupCode(entity.getGroupCode())
                .racingDistance(entity.getRacingDistance())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}