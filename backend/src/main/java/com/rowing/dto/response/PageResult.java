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
public class PageResult<T> {

    private List<T> data;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;

    public static <T> PageResult<T> of(List<T> data, Long total, Integer pageNum, Integer pageSize) {
        return PageResult.<T>builder()
                .data(data)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }
}