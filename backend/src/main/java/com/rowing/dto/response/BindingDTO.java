package com.rowing.dto.response;

import com.rowing.entity.BracketBinding;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindingDTO {

    private Long id;
    private Long bracketId;
    private String bracketCode;
    private Integer bracketMinDistance;
    private Integer bracketMaxDistance;
    private Integer bracketStatus;
    private Long groupId;
    private String groupName;
    private String groupCode;
    private Integer racingDistance;
    private LocalDateTime bindingTime;
    private Integer status;

    public static BindingDTO fromEntity(BracketBinding entity) {
        return BindingDTO.builder()
                .id(entity.getId())
                .bracketId(entity.getBracketId())
                .groupId(entity.getGroupId())
                .bindingTime(entity.getBindingTime())
                .status(entity.getStatus())
                .build();
    }
}