package com.rowing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BracketQueryRequest {

    private String bracketCode;

    private Integer minDistance;

    private Integer maxDistance;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}