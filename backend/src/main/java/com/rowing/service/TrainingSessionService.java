package com.rowing.service;

import com.rowing.dto.request.SessionCreateRequest;
import com.rowing.dto.request.SessionUpdateRequest;
import com.rowing.dto.response.SessionDTO;

import java.time.LocalDate;
import java.util.List;

public interface TrainingSessionService {

    SessionDTO create(SessionCreateRequest request);

    SessionDTO update(SessionUpdateRequest request);

    void delete(Long id);

    SessionDTO getById(Long id);

    List<SessionDTO> query(LocalDate startDate, LocalDate endDate);
}
