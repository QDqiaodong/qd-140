package com.rowing.controller;

import com.rowing.dto.request.GroupCreateRequest;
import com.rowing.dto.request.GroupUpdateRequest;
import com.rowing.dto.response.ApiResponse;
import com.rowing.dto.response.GroupDTO;
import com.rowing.dto.response.GroupUpdateResultDTO;
import com.rowing.dto.response.PageResult;
import com.rowing.service.RowingGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class RowingGroupController {

    private final RowingGroupService groupService;

    @PostMapping
    public ApiResponse<GroupDTO> create(@Valid @RequestBody GroupCreateRequest request) {
        return ApiResponse.success(groupService.create(request));
    }

    @PutMapping
    public ApiResponse<GroupUpdateResultDTO> update(@Valid @RequestBody GroupUpdateRequest request) {
        return ApiResponse.success(groupService.update(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        groupService.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<GroupDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(groupService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ApiResponse<GroupDTO> getByCode(@PathVariable String code) {
        return ApiResponse.success(groupService.getByCode(code));
    }

    @GetMapping("/list")
    public ApiResponse<List<GroupDTO>> list() {
        return ApiResponse.success(groupService.findAll());
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<GroupDTO>> page(
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String groupCode,
            @RequestParam(required = false) Integer racingDistance,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        return ApiResponse.success(groupService.query(groupName, groupCode, racingDistance, pageNum, pageSize));
    }

    @GetMapping("/distance/{distance}")
    public ApiResponse<List<GroupDTO>> getByDistance(@PathVariable Integer distance) {
        return ApiResponse.success(groupService.findByRacingDistance(distance));
    }

    @GetMapping("/distances")
    public ApiResponse<List<Integer>> getDistinctDistances() {
        return ApiResponse.success(groupService.findDistinctRacingDistances());
    }

    @PostMapping(value = "/{id}/plan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<GroupDTO> uploadPlan(@PathVariable Long id,
                                            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("训练计划上传成功", groupService.uploadPlan(id, file));
    }

    @GetMapping("/{id}/plan")
    public ResponseEntity<Resource> downloadPlan(@PathVariable Long id) {
        RowingGroupService.PlanFile plan = groupService.loadPlan(id);
        String encodedName = URLEncoder.encode(plan.fileName(), StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName)
                .contentLength(plan.size())
                .body(plan.resource());
    }
}