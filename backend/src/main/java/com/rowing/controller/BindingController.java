package com.rowing.controller;

import com.rowing.dto.request.BindingRequest;
import com.rowing.dto.response.ApiResponse;
import com.rowing.dto.response.BindingDTO;
import com.rowing.dto.response.ChangeLogDTO;
import com.rowing.dto.response.PageResult;
import com.rowing.service.BindingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/binding")
@RequiredArgsConstructor
public class BindingController {

    private final BindingService bindingService;

    @PostMapping("/bind")
    public ApiResponse<BindingDTO> bind(@Valid @RequestBody BindingRequest request) {
        return ApiResponse.success(bindingService.bind(request));
    }

    @PostMapping("/unbind")
    public ApiResponse<BindingDTO> unbind(@Valid @RequestBody BindingRequest request) {
        return ApiResponse.success(bindingService.unbind(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BindingDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(bindingService.getById(id));
    }

    @GetMapping("/bracket/{bracketId}")
    public ApiResponse<List<BindingDTO>> getByBracketId(@PathVariable Long bracketId) {
        return ApiResponse.success(bindingService.findByBracketId(bracketId));
    }

    @GetMapping("/group/{groupId}")
    public ApiResponse<List<BindingDTO>> getByGroupId(@PathVariable Long groupId) {
        return ApiResponse.success(bindingService.findByGroupId(groupId));
    }

    @GetMapping("/active")
    public ApiResponse<List<BindingDTO>> getAllActive() {
        return ApiResponse.success(bindingService.findAllActive());
    }

    @GetMapping("/all")
    public ApiResponse<List<BindingDTO>> getAll() {
        return ApiResponse.success(bindingService.findAll());
    }

    @GetMapping("/logs/bracket/{bracketId}")
    public ApiResponse<List<ChangeLogDTO>> getLogsByBracketId(@PathVariable Long bracketId) {
        return ApiResponse.success(bindingService.getLogsByBracketId(bracketId));
    }

    @GetMapping("/logs/group/{groupId}")
    public ApiResponse<List<ChangeLogDTO>> getLogsByGroupId(@PathVariable Long groupId) {
        return ApiResponse.success(bindingService.getLogsByGroupId(groupId));
    }

    @GetMapping("/logs/page")
    public ApiResponse<PageResult<ChangeLogDTO>> queryLogs(
            @RequestParam(required = false) String bracketCode,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String changeType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        return ApiResponse.success(bindingService.queryLogs(bracketCode, groupName, changeType, pageNum, pageSize));
    }

    @GetMapping("/logs/recent")
    public ApiResponse<List<ChangeLogDTO>> getRecentLogs(@RequestParam(defaultValue = "20") Integer limit) {
        return ApiResponse.success(bindingService.getRecentLogs(limit));
    }
}