package com.rowing.service.impl;

import com.rowing.dto.response.BracketDTO;
import com.rowing.dto.response.DistanceStatDTO;
import com.rowing.dto.response.GroupDTO;
import com.rowing.repository.BracketBindingRepository;
import com.rowing.service.DockingBracketService;
import com.rowing.service.DistanceStatService;
import com.rowing.service.RowingGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistanceStatServiceImpl implements DistanceStatService {

    private final RowingGroupService groupService;
    private final DockingBracketService bracketService;
    private final BracketBindingRepository bindingRepository;

    @Override
    public List<DistanceStatDTO> getAllDistanceStats() {
        List<Integer> distances = groupService.findDistinctRacingDistances();
        return distances.stream()
                .map(this::buildStatDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DistanceStatDTO getDistanceStat(Integer distance) {
        return buildStatDTO(distance);
    }

    @Override
    public List<DistanceStatDTO> getDistanceStatsByRange(Integer minDistance, Integer maxDistance) {
        List<Integer> distances = groupService.findDistinctRacingDistances();
        return distances.stream()
                .filter(d -> d >= minDistance && d <= maxDistance)
                .map(this::buildStatDTO)
                .collect(Collectors.toList());
    }

    private DistanceStatDTO buildStatDTO(Integer distance) {
        List<BracketDTO> brackets = bracketService.findByDistanceRange(distance);
        List<GroupDTO> groups = groupService.findByRacingDistance(distance);
        Long bindingCount = bindingRepository.countByDistanceRange(distance);

        return DistanceStatDTO.builder()
                .distance(distance)
                .distanceLabel(distance + "m")
                .bracketCount(brackets.size())
                .groupCount(groups.size())
                .brackets(brackets)
                .groups(groups)
                .build();
    }
}