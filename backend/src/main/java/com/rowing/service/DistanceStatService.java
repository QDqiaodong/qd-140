package com.rowing.service;

import com.rowing.dto.response.DistanceStatDTO;

import java.util.List;

public interface DistanceStatService {

    List<DistanceStatDTO> getAllDistanceStats();

    DistanceStatDTO getDistanceStat(Integer distance);

    List<DistanceStatDTO> getDistanceStatsByRange(Integer minDistance, Integer maxDistance);
}