package com.rowing.service;

import com.rowing.dto.request.BindingRequest;
import com.rowing.dto.response.BindingDTO;
import com.rowing.dto.response.ChangeLogDTO;
import com.rowing.dto.response.PageResult;

import java.util.List;

public interface BindingService {

    BindingDTO bind(BindingRequest request);

    BindingDTO unbind(BindingRequest request);

    BindingDTO getById(Long id);

    List<BindingDTO> findByBracketId(Long bracketId);

    List<BindingDTO> findByGroupId(Long groupId);

    List<BindingDTO> findAllActive();

    List<BindingDTO> findAll();

    List<ChangeLogDTO> getLogsByBracketId(Long bracketId);

    List<ChangeLogDTO> getLogsByGroupId(Long groupId);

    PageResult<ChangeLogDTO> queryLogs(String bracketCode, String groupName, String changeType, Integer pageNum, Integer pageSize);

    List<ChangeLogDTO> getRecentLogs(Integer limit);
}