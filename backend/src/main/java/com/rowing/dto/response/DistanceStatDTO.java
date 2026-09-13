package com.rowing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceStatDTO {

    private Integer distance;
    private String distanceLabel;
    private Integer bracketCount;
    private Integer groupCount;
    private List<BracketDTO> brackets;
    private List<GroupDTO> groups;
}