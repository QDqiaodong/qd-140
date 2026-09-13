package com.rowing.service;

import com.rowing.dto.request.BracketCreateRequest;
import com.rowing.dto.request.BracketQueryRequest;
import com.rowing.dto.request.BracketUpdateRequest;
import com.rowing.dto.response.BracketDTO;
import com.rowing.dto.response.PageResult;

import java.util.List;

public interface DockingBracketService {

    BracketDTO create(BracketCreateRequest request);

    BracketDTO update(BracketUpdateRequest request);

    void delete(Long id);

    BracketDTO getById(Long id);

    BracketDTO getByCode(String bracketCode);

    PageResult<BracketDTO> query(BracketQueryRequest request);

    List<BracketDTO> findAllEnabled();

    List<BracketDTO> findByDistanceRange(Integer distance);
}