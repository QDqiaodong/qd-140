package com.rowing.service;

import com.rowing.dto.request.GroupCreateRequest;
import com.rowing.dto.request.GroupUpdateRequest;
import com.rowing.dto.response.GroupDTO;
import com.rowing.dto.response.GroupUpdateResultDTO;
import com.rowing.dto.response.PageResult;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RowingGroupService {

    GroupDTO create(GroupCreateRequest request);

    GroupUpdateResultDTO update(GroupUpdateRequest request);

    void delete(Long id);

    GroupDTO getById(Long id);

    GroupDTO getByCode(String groupCode);

    PageResult<GroupDTO> query(String groupName, String groupCode, Integer racingDistance, Integer pageNum, Integer pageSize);

    List<GroupDTO> findAll();

    List<GroupDTO> findByRacingDistance(Integer distance);

    List<Integer> findDistinctRacingDistances();

    GroupDTO uploadPlan(Long groupId, MultipartFile file);

    PlanFile loadPlan(Long groupId);

    record PlanFile(Resource resource, String fileName, long size) {
    }
}