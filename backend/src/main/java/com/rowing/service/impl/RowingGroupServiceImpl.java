package com.rowing.service.impl;

import com.rowing.dto.request.GroupCreateRequest;
import com.rowing.dto.request.GroupUpdateRequest;
import com.rowing.dto.response.GroupDTO;
import com.rowing.dto.response.PageResult;
import com.rowing.entity.BindingChangeLog;
import com.rowing.entity.BracketBinding;
import com.rowing.entity.DockingBracket;
import com.rowing.entity.RowingGroup;
import com.rowing.exception.BusinessException;
import com.rowing.repository.BindingChangeLogRepository;
import com.rowing.repository.BracketBindingRepository;
import com.rowing.repository.DockingBracketRepository;
import com.rowing.repository.RowingGroupRepository;
import com.rowing.service.RowingGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RowingGroupServiceImpl implements RowingGroupService {

    private final RowingGroupRepository groupRepository;
    private final BracketBindingRepository bindingRepository;
    private final DockingBracketRepository bracketRepository;
    private final BindingChangeLogRepository changeLogRepository;

    @Override
    @Transactional
    public GroupDTO create(GroupCreateRequest request) {
        if (groupRepository.existsByGroupCode(request.getGroupCode())) {
            throw new BusinessException("组别编码已存在");
        }

        RowingGroup group = RowingGroup.builder()
                .groupName(request.getGroupName())
                .groupCode(request.getGroupCode())
                .racingDistance(request.getRacingDistance())
                .description(request.getDescription())
                .build();

        group = groupRepository.save(group);
        log.info("创建组别成功: {}", group.getGroupCode());
        return GroupDTO.fromEntity(group);
    }

    @Override
    @Transactional
    public GroupDTO update(GroupUpdateRequest request) {
        RowingGroup group = groupRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("组别不存在"));

        Integer oldDistance = group.getRacingDistance();

        if (request.getGroupName() != null) {
            group.setGroupName(request.getGroupName());
        }
        if (request.getGroupCode() != null && !request.getGroupCode().equals(group.getGroupCode())) {
            if (groupRepository.existsByGroupCode(request.getGroupCode())) {
                throw new BusinessException("组别编码已存在");
            }
            group.setGroupCode(request.getGroupCode());
        }
        if (request.getRacingDistance() != null) {
            group.setRacingDistance(request.getRacingDistance());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }

        Integer newDistance = group.getRacingDistance();

        if (!oldDistance.equals(newDistance)) {
            List<BracketBinding> bindings = bindingRepository.findByGroupId(request.getId());
            for (BracketBinding binding : bindings) {
                if (binding.getStatus() == 1) {
                    DockingBracket bracket = bracketRepository.findById(binding.getBracketId()).orElse(null);
                    if (bracket != null) {
                        BindingChangeLog logEntry = BindingChangeLog.builder()
                                .bracketId(bracket.getId())
                                .bracketCode(bracket.getBracketCode())
                                .groupId(group.getId())
                                .groupName(group.getGroupName())
                                .changeType("UPDATE")
                                .previousDistance(oldDistance)
                                .newDistance(newDistance)
                                .changeReason("组别竞速距离变更")
                                .operator("system")
                                .build();
                        changeLogRepository.save(logEntry);

                        if (newDistance < bracket.getMinDistance() || newDistance > bracket.getMaxDistance()) {
                            binding.setStatus(0);
                            bindingRepository.save(binding);
                        }
                    }
                }
            }
        }

        group = groupRepository.save(group);
        log.info("更新组别成功: {}", group.getGroupCode());
        return GroupDTO.fromEntity(group);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RowingGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("组别不存在"));

        bindingRepository.deactivateByGroupId(id);
        groupRepository.delete(group);

        log.info("删除组别成功: {}", group.getGroupCode());
    }

    @Override
    public GroupDTO getById(Long id) {
        RowingGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("组别不存在"));
        return GroupDTO.fromEntity(group);
    }

    @Override
    public GroupDTO getByCode(String groupCode) {
        RowingGroup group = groupRepository.findByGroupCode(groupCode)
                .orElseThrow(() -> new BusinessException("组别不存在"));
        return GroupDTO.fromEntity(group);
    }

    @Override
    public PageResult<GroupDTO> query(String groupName, String groupCode, Integer racingDistance, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<RowingGroup> page = groupRepository.findByConditions(groupName, groupCode, racingDistance, pageable);

        List<GroupDTO> dtoList = page.getContent().stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    public List<GroupDTO> findAll() {
        return groupRepository.findAll().stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupDTO> findByRacingDistance(Integer distance) {
        return groupRepository.findByRacingDistance(distance).stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> findDistinctRacingDistances() {
        return groupRepository.findDistinctRacingDistances();
    }
}