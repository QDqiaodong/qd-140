package com.rowing.service.impl;

import com.rowing.dto.request.BindingRequest;
import com.rowing.dto.response.BindingDTO;
import com.rowing.dto.response.ChangeLogDTO;
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
import com.rowing.service.BindingService;
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
public class BindingServiceImpl implements BindingService {

    private final BracketBindingRepository bindingRepository;
    private final DockingBracketRepository bracketRepository;
    private final RowingGroupRepository groupRepository;
    private final BindingChangeLogRepository changeLogRepository;

    @Override
    @Transactional
    public BindingDTO bind(BindingRequest request) {
        DockingBracket bracket = bracketRepository.findById(request.getBracketId())
                .orElseThrow(() -> new BusinessException("支架不存在"));

        if (bracket.getStatus() != 1) {
            throw new BusinessException("支架已禁用");
        }

        RowingGroup group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new BusinessException("组别不存在"));

        if (group.getRacingDistance() < bracket.getMinDistance() || group.getRacingDistance() > bracket.getMaxDistance()) {
            throw new BusinessException("组别竞速距离超出支架适配范围");
        }

        // 曾因区间不匹配被拆掉（失效）的绑定，重新匹配时恢复生效，不再占用唯一键导致无法重绑
        BracketBinding binding = bindingRepository
                .findByBracketIdAndGroupId(request.getBracketId(), request.getGroupId())
                .orElse(null);
        boolean reactivated = false;
        if (binding != null) {
            if (binding.getStatus() == 1) {
                throw new BusinessException("绑定关系已存在");
            }
            binding.setStatus(1);
            binding = bindingRepository.save(binding);
            reactivated = true;
        } else {
            binding = bindingRepository.save(BracketBinding.builder()
                    .bracketId(request.getBracketId())
                    .groupId(request.getGroupId())
                    .status(1)
                    .build());
        }

        BindingChangeLog logEntry = BindingChangeLog.builder()
                .bracketId(bracket.getId())
                .bracketCode(bracket.getBracketCode())
                .groupId(group.getId())
                .groupName(group.getGroupName())
                .changeType("BIND")
                .previousDistance(null)
                .newDistance(group.getRacingDistance())
                .changeReason(request.getReason() != null ? request.getReason()
                        : (reactivated ? "区间重新匹配，恢复绑定" : "手动绑定"))
                .operator(request.getOperator() != null ? request.getOperator() : "admin")
                .build();

        changeLogRepository.save(logEntry);
        log.info("绑定成功: {} -> {}", bracket.getBracketCode(), group.getGroupName());

        return buildBindingDTO(binding, bracket, group);
    }

    @Override
    @Transactional
    public BindingDTO unbind(BindingRequest request) {
        BracketBinding binding = bindingRepository.findByBracketIdAndGroupId(request.getBracketId(), request.getGroupId())
                .orElseThrow(() -> new BusinessException("绑定关系不存在"));

        if (binding.getStatus() != 1) {
            throw new BusinessException("绑定关系已失效");
        }

        DockingBracket bracket = bracketRepository.findById(request.getBracketId()).orElse(null);
        RowingGroup group = groupRepository.findById(request.getGroupId()).orElse(null);

        binding.setStatus(0);
        binding = bindingRepository.save(binding);

        BindingChangeLog logEntry = BindingChangeLog.builder()
                .bracketId(bracket != null ? bracket.getId() : request.getBracketId())
                .bracketCode(bracket != null ? bracket.getBracketCode() : null)
                .groupId(group != null ? group.getId() : request.getGroupId())
                .groupName(group != null ? group.getGroupName() : null)
                .changeType("UNBIND")
                .previousDistance(group != null ? group.getRacingDistance() : null)
                .newDistance(null)
                .changeReason(request.getReason() != null ? request.getReason() : "手动解绑")
                .operator(request.getOperator() != null ? request.getOperator() : "admin")
                .build();

        changeLogRepository.save(logEntry);
        log.info("解绑成功: {} -> {}", bracket != null ? bracket.getBracketCode() : request.getBracketId(),
                group != null ? group.getGroupName() : request.getGroupId());

        return buildBindingDTO(binding, bracket, group);
    }

    @Override
    public BindingDTO getById(Long id) {
        BracketBinding binding = bindingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("绑定关系不存在"));

        DockingBracket bracket = bracketRepository.findById(binding.getBracketId()).orElse(null);
        RowingGroup group = groupRepository.findById(binding.getGroupId()).orElse(null);

        return buildBindingDTO(binding, bracket, group);
    }

    @Override
    public List<BindingDTO> findByBracketId(Long bracketId) {
        List<BracketBinding> bindings = bindingRepository.findByBracketId(bracketId);
        return bindings.stream()
                .map(binding -> {
                    DockingBracket bracket = bracketRepository.findById(binding.getBracketId()).orElse(null);
                    RowingGroup group = groupRepository.findById(binding.getGroupId()).orElse(null);
                    return buildBindingDTO(binding, bracket, group);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BindingDTO> findByGroupId(Long groupId) {
        List<BracketBinding> bindings = bindingRepository.findByGroupId(groupId);
        return bindings.stream()
                .map(binding -> {
                    DockingBracket bracket = bracketRepository.findById(binding.getBracketId()).orElse(null);
                    RowingGroup group = groupRepository.findById(binding.getGroupId()).orElse(null);
                    return buildBindingDTO(binding, bracket, group);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BindingDTO> findAllActive() {
        List<BracketBinding> bindings = bindingRepository.findAllActive();
        return bindings.stream()
                .map(binding -> {
                    DockingBracket bracket = bracketRepository.findById(binding.getBracketId()).orElse(null);
                    RowingGroup group = groupRepository.findById(binding.getGroupId()).orElse(null);
                    return buildBindingDTO(binding, bracket, group);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BindingDTO> findAll() {
        // 包含已失效的绑定，绑定列表刷新后失效关系仍可见并展示为“失效”
        return bindingRepository.findAll().stream()
                .map(binding -> {
                    DockingBracket bracket = bracketRepository.findById(binding.getBracketId()).orElse(null);
                    RowingGroup group = groupRepository.findById(binding.getGroupId()).orElse(null);
                    return buildBindingDTO(binding, bracket, group);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ChangeLogDTO> getLogsByBracketId(Long bracketId) {
        return changeLogRepository.findByBracketId(bracketId).stream()
                .map(ChangeLogDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChangeLogDTO> getLogsByGroupId(Long groupId) {
        return changeLogRepository.findByGroupId(groupId).stream()
                .map(ChangeLogDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ChangeLogDTO> queryLogs(String bracketCode, String groupName, String changeType, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "changedAt")
        );

        Page<BindingChangeLog> page = changeLogRepository.findByConditions(bracketCode, groupName, changeType, pageable);

        List<ChangeLogDTO> dtoList = page.getContent().stream()
                .map(ChangeLogDTO::fromEntity)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, page.getTotalElements(), pageNum, pageSize);
    }

    @Override
    public List<ChangeLogDTO> getRecentLogs(Integer limit) {
        return changeLogRepository.findRecentLogs(limit).stream()
                .map(ChangeLogDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private BindingDTO buildBindingDTO(BracketBinding binding, DockingBracket bracket, RowingGroup group) {
        return BindingDTO.builder()
                .id(binding.getId())
                .bracketId(binding.getBracketId())
                .bracketCode(bracket != null ? bracket.getBracketCode() : null)
                .bracketMinDistance(bracket != null ? bracket.getMinDistance() : null)
                .bracketMaxDistance(bracket != null ? bracket.getMaxDistance() : null)
                .bracketStatus(bracket != null ? bracket.getStatus() : null)
                .groupId(binding.getGroupId())
                .groupName(group != null ? group.getGroupName() : null)
                .groupCode(group != null ? group.getGroupCode() : null)
                .racingDistance(group != null ? group.getRacingDistance() : null)
                .bindingTime(binding.getBindingTime())
                .status(binding.getStatus())
                .build();
    }
}