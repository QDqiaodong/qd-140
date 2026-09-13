package com.rowing.controller;

import com.rowing.dto.response.ApiResponse;
import com.rowing.dto.response.DistanceStatDTO;
import com.rowing.service.DistanceStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stat")
@RequiredArgsConstructor
public class DistanceStatController {

    private final DistanceStatService statService;

    @GetMapping("/distance/all")
    public ApiResponse<List<DistanceStatDTO>> getAllDistanceStats() {
        return ApiResponse.success(statService.getAllDistanceStats());
    }

    @GetMapping("/distance/{distance}")
    public ApiResponse<DistanceStatDTO> getDistanceStat(@PathVariable Integer distance) {
        return ApiResponse.success(statService.getDistanceStat(distance));
    }

    @GetMapping("/distance/range")
    public ApiResponse<List<DistanceStatDTO>> getDistanceStatsByRange(
            @RequestParam Integer minDistance,
            @RequestParam Integer maxDistance) {

        return ApiResponse.success(statService.getDistanceStatsByRange(minDistance, maxDistance));
    }
}