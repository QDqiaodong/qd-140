package com.rowing.service;

import com.rowing.dto.request.GroupCreateRequest;
import com.rowing.dto.request.GroupUpdateRequest;
import com.rowing.dto.response.GroupDTO;
import com.rowing.dto.response.PageResult;

import java.util.List;

public interface RowingGroupService {

    GroupDTO create(GroupCreateRequest request);

    GroupDTO update(GroupUpdateRequest request);

    void delete(Long id);

    GroupDTO getById(Long id);

    GroupDTO getByCode(String groupCode);

    PageResult<GroupDTO> query(String groupName, String groupCode, Integer racingDistance, Integer pageNum, Integer pageSize);

    List<GroupDTO> findAll();

    List<GroupDTO> findByRacingDistance(Integer distance);

    List<Integer> findDistinctRacingDistances();
}