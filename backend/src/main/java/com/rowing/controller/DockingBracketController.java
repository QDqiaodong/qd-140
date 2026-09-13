package com.rowing.controller;

import com.rowing.dto.request.BracketCreateRequest;
import com.rowing.dto.request.BracketQueryRequest;
import com.rowing.dto.request.BracketUpdateRequest;
import com.rowing.dto.response.ApiResponse;
import com.rowing.dto.response.BracketDTO;
import com.rowing.dto.response.PageResult;
import com.rowing.service.DockingBracketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bracket")
@RequiredArgsConstructor
public class DockingBracketController {

    private final DockingBracketService bracketService;

    @PostMapping
    public ApiResponse<BracketDTO> create(@Valid @RequestBody BracketCreateRequest request) {
        return ApiResponse.success(bracketService.create(request));
    }

    @PutMapping
    public ApiResponse<BracketDTO> update(@Valid @RequestBody BracketUpdateRequest request) {
        return ApiResponse.success(bracketService.update(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bracketService.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<BracketDTO> getById(@PathVariable Long id) {
        return ApiResponse.success(bracketService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ApiResponse<BracketDTO> getByCode(@PathVariable String code) {
        return ApiResponse.success(bracketService.getByCode(code));
    }

    @GetMapping("/list")
    public ApiResponse<List<BracketDTO>> list() {
        return ApiResponse.success(bracketService.findAllEnabled());
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("OK");
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<BracketDTO>> page(
            @RequestParam(required = false) String bracketCode,
            @RequestParam(required = false) Integer minDistance,
            @RequestParam(required = false) Integer maxDistance,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        BracketQueryRequest request = BracketQueryRequest.builder()
                .bracketCode(bracketCode)
                .minDistance(minDistance)
                .maxDistance(maxDistance)
                .status(status)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();

        return ApiResponse.success(bracketService.query(request));
    }

    @GetMapping("/distance/{distance}")
    public ApiResponse<List<BracketDTO>> getByDistance(@PathVariable Integer distance) {
        return ApiResponse.success(bracketService.findByDistanceRange(distance));
    }
}