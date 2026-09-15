package com.rowing.controller;

import com.rowing.dto.request.SessionCreateRequest;
import com.rowing.dto.request.SessionUpdateRequest;
import com.rowing.dto.response.ApiResponse;
import com.rowing.dto.response.SessionDTO;
import com.rowing.service.TrainingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class TrainingSessionController {

    private final TrainingSessionService sessionService;

    @PostMapping
    public ApiResponse<SessionDTO> create(@Valid @RequestBody SessionCreateRequest request) {
        return ApiResponse.success(sessionService.create(request));
    }

    @PutMapping
    public ApiResponse<SessionDTO> update(@Valid @RequestBody SessionUpdateRequest request) {
        return ApiResponse.success(sessionService.update(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sessionService.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<SessionDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(sessionService.getById(id));
    }

    @GetMapping("/list")
    public ApiResponse<List<SessionDTO>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(sessionService.query(startDate, endDate));
    }
}
