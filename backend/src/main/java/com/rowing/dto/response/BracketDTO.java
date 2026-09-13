package com.rowing.dto.response;

import com.rowing.entity.DockingBracket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BracketDTO {

    private Long id;
    private String bracketCode;
    private BigDecimal loadCapacity;
    private Integer minDistance;
    private Integer maxDistance;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BracketDTO fromEntity(DockingBracket entity) {
        return BracketDTO.builder()
                .id(entity.getId())
                .bracketCode(entity.getBracketCode())
                .loadCapacity(entity.getLoadCapacity())
                .minDistance(entity.getMinDistance())
                .maxDistance(entity.getMaxDistance())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}