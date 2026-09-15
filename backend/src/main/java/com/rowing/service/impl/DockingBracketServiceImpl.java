package com.rowing.service.impl;

import com.rowing.dto.request.BracketCreateRequest;
import com.rowing.dto.request.BracketQueryRequest;
import com.rowing.dto.request.BracketUpdateRequest;
import com.rowing.dto.response.BracketDTO;
import com.rowing.dto.response.PageResult;
import com.rowing.entity.DockingBracket;
import com.rowing.entity.TrainingSession;
import com.rowing.exception.BusinessException;
import com.rowing.repository.BracketBindingRepository;
import com.rowing.repository.DockingBracketRepository;
import com.rowing.repository.TrainingSessionRepository;
import com.rowing.service.DockingBracketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockingBracketServiceImpl implements DockingBracketService {

    private final DockingBracketRepository bracketRepository;
    private final BracketBindingRepository bindingRepository;
    private final TrainingSessionRepository sessionRepository;

    @Override
    @Transactional
    public BracketDTO create(BracketCreateRequest request) {
        if (bracketRepository.existsByBracketCode(request.getBracketCode())) {
            throw new BusinessException("支架编号已存在");
        }

        if (request.getMinDistance() > request.getMaxDistance()) {
            throw new BusinessException("最小距离不能大于最大距离");
        }

        DockingBracket bracket = DockingBracket.builder()
                .bracketCode(request.getBracketCode())
                .loadCapacity(request.getLoadCapacity())
                .minDistance(request.getMinDistance())
                .maxDistance(request.getMaxDistance())
                .remark(request.getRemark())
                .status(1)
                .build();

        bracket = bracketRepository.save(bracket);
        log.info("创建支架成功: {}", bracket.getBracketCode());
        return BracketDTO.fromEntity(bracket);
    }

    @Override
    @Transactional
    public BracketDTO update(BracketUpdateRequest request) {
        DockingBracket bracket = bracketRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("支架不存在"));

        if (request.getBracketCode() != null && !request.getBracketCode().equals(bracket.getBracketCode())) {
            if (bracketRepository.existsByBracketCode(request.getBracketCode())) {
                throw new BusinessException("支架编号已存在");
            }
            bracket.setBracketCode(request.getBracketCode());
        }

        if (request.getLoadCapacity() != null) {
            bracket.setLoadCapacity(request.getLoadCapacity());
        }
        if (request.getMinDistance() != null) {
            bracket.setMinDistance(request.getMinDistance());
        }
        if (request.getMaxDistance() != null) {
            bracket.setMaxDistance(request.getMaxDistance());
        }
        if (request.getStatus() != null) {
            bracket.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            bracket.setRemark(request.getRemark());
        }

        if (bracket.getMinDistance() > bracket.getMaxDistance()) {
            throw new BusinessException("最小距离不能大于最大距离");
        }

        bracket = bracketRepository.save(bracket);

        // 停用保存成功后，同一事务内把该支架上还没上课（含今天）的有效课次标掉；
        // 已过上课日的课保留当时记录不动。重新启用（status=1）不进入此分支，已标掉的课次不会自动恢复。
        if (bracket.getStatus() != null && bracket.getStatus() == 0) {
            markUpcomingSessionsDisabled(bracket);
        }

        log.info("更新支架成功: {}", bracket.getBracketCode());
        return BracketDTO.fromEntity(bracket);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DockingBracket bracket = bracketRepository.findById(id)
                .orElseThrow(() -> new BusinessException("支架不存在"));

        bindingRepository.deactivateByBracketId(id);

        bracket.setStatus(0);
        bracketRepository.save(bracket);
        // 删除（软删）视同停用：未上课次一并标掉，保留已过上课日的历史记录
        markUpcomingSessionsDisabled(bracket);
        log.info("删除支架成功: {}", bracket.getBracketCode());
    }

    /**
     * 支架停用后，其上课日还没到或就是今天、且当前仍有效的课次一律标为“因支架停用失效”（status=0）。
     * 标掉是持久化的：之后即使支架重新启用，这些课次也不会自动变回有效，须场务在排课页改课重排。
     */
    private void markUpcomingSessionsDisabled(DockingBracket bracket) {
        List<TrainingSession> upcoming = sessionRepository
                .findByBracketIdAndStatusAndSessionDateGreaterThanEqual(bracket.getId(), 1, LocalDate.now());
        if (upcoming.isEmpty()) {
            return;
        }
        upcoming.forEach(session -> session.setStatus(0));
        sessionRepository.saveAll(upcoming);
        log.info("支架 {} 已停用，{} 节未上课次标记为不可上（支架已停用）", bracket.getBracketCode(), upcoming.size());
    }

    @Override
    public BracketDTO getById(Long id) {
        DockingBracket bracket = bracketRepository.findById(id)
                .orElseThrow(() -> new BusinessException("支架不存在"));
        return BracketDTO.fromEntity(bracket);
    }

    @Override
    public BracketDTO getByCode(String bracketCode) {
        DockingBracket bracket = bracketRepository.findByBracketCode(bracketCode)
                .orElseThrow(() -> new BusinessException("支架不存在"));
        return BracketDTO.fromEntity(bracket);
    }

    @Override
    public PageResult<BracketDTO> query(BracketQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPageNum() - 1,
                request.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<DockingBracket> page = bracketRepository.findByConditions(
                request.getBracketCode(),
                request.getMinDistance(),
                request.getMaxDistance(),
                request.getStatus(),
                pageable
        );

        List<BracketDTO> dtoList = page.getContent().stream()
                .map(BracketDTO::fromEntity)
                .collect(Collectors.toList());

        return PageResult.of(dtoList, page.getTotalElements(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public List<BracketDTO> findAllEnabled() {
        return bracketRepository.findAllEnabled().stream()
                .map(BracketDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BracketDTO> findByDistanceRange(Integer distance) {
        return bracketRepository.findByDistanceRange(distance).stream()
                .map(BracketDTO::fromEntity)
                .collect(Collectors.toList());
    }
}