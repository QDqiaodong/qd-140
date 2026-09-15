package com.rowing.service.impl;

import com.rowing.dto.request.SessionCreateRequest;
import com.rowing.dto.request.SessionUpdateRequest;
import com.rowing.dto.response.SessionDTO;
import com.rowing.entity.DockingBracket;
import com.rowing.entity.RowingGroup;
import com.rowing.entity.TrainingSession;
import com.rowing.exception.BusinessException;
import com.rowing.repository.DockingBracketRepository;
import com.rowing.repository.RowingGroupRepository;
import com.rowing.repository.TrainingSessionRepository;
import com.rowing.service.TrainingSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingSessionServiceImpl implements TrainingSessionService {

    private final TrainingSessionRepository sessionRepository;
    private final DockingBracketRepository bracketRepository;
    private final RowingGroupRepository groupRepository;

    @Override
    @Transactional
    public SessionDTO create(SessionCreateRequest request) {
        DockingBracket bracket = checkBracket(request.getBracketId());
        RowingGroup group = checkGroup(request.getGroupId());
        checkCapacity(bracket, request.getExpectedPersonCount());

        TrainingSession session = TrainingSession.builder()
                .sessionDate(request.getSessionDate())
                .bracketId(request.getBracketId())
                .groupId(request.getGroupId())
                .expectedPersonCount(request.getExpectedPersonCount())
                .remark(request.getRemark())
                .build();

        session = sessionRepository.save(session);
        log.info("排课成功: {} 日期={} 人数={}", bracket.getBracketCode(),
                session.getSessionDate(), session.getExpectedPersonCount());
        return toDTO(session, bracket, group);
    }

    @Override
    @Transactional
    public SessionDTO update(SessionUpdateRequest request) {
        TrainingSession session = sessionRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("训练课次不存在"));

        // 改课时一律按所选支架“当前承重”拦截，旧课若已超载，不把人数降下来就保存不了
        DockingBracket bracket = checkBracket(request.getBracketId());
        RowingGroup group = checkGroup(request.getGroupId());
        checkCapacity(bracket, request.getExpectedPersonCount());

        session.setSessionDate(request.getSessionDate());
        session.setBracketId(request.getBracketId());
        session.setGroupId(request.getGroupId());
        session.setExpectedPersonCount(request.getExpectedPersonCount());
        session.setRemark(request.getRemark());
        // 改课即重排：支架、承重校验都过了，此前因支架停用被标掉的课次在保存后恢复为有效
        session.setStatus(1);

        session = sessionRepository.save(session);
        log.info("更新课次成功: id={} 支架={} 人数={}", session.getId(),
                bracket.getBracketCode(), session.getExpectedPersonCount());
        return toDTO(session, bracket, group);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TrainingSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("训练课次不存在"));
        sessionRepository.delete(session);
        log.info("删除课次成功: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDTO getById(Long id) {
        TrainingSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("训练课次不存在"));
        return toDTO(session,
                bracketRepository.findById(session.getBracketId()).orElse(null),
                session.getGroupId() == null ? null : groupRepository.findById(session.getGroupId()).orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDTO> query(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.of(1970, 1, 1);
        LocalDate end = endDate != null ? endDate : LocalDate.of(9999, 12, 31);

        return sessionRepository
                .findBySessionDateBetweenOrderBySessionDateAscIdAsc(start, end)
                .stream()
                .map(session -> toDTO(session,
                        bracketRepository.findById(session.getBracketId()).orElse(null),
                        session.getGroupId() == null ? null : groupRepository.findById(session.getGroupId()).orElse(null)))
                .collect(Collectors.toList());
    }

    private DockingBracket checkBracket(Long bracketId) {
        if (bracketId == null) {
            throw new BusinessException("请选择停靠支架");
        }
        DockingBracket bracket = bracketRepository.findById(bracketId)
                .orElseThrow(() -> new BusinessException("停靠支架不存在"));
        if (bracket.getStatus() == null || bracket.getStatus() != 1) {
            throw new BusinessException("支架 " + bracket.getBracketCode() + " 已禁用，不能排课");
        }
        return bracket;
    }

    private RowingGroup checkGroup(Long groupId) {
        if (groupId == null) {
            return null;
        }
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("训练组别不存在"));
    }

    /**
     * 人数必填且不得超过支架当前承重；超过直接拦住，并写清承重和本次人数。
     */
    private void checkCapacity(DockingBracket bracket, Integer expectedPersonCount) {
        if (expectedPersonCount == null) {
            throw new BusinessException("预计上艇人数不能为空，人数空着不能排课");
        }
        if (expectedPersonCount <= 0) {
            throw new BusinessException("预计上艇人数必须大于0");
        }
        BigDecimal capacity = bracket.getLoadCapacity();
        if (new BigDecimal(expectedPersonCount).compareTo(capacity) > 0) {
            throw new BusinessException(String.format(
                    "预计上艇人数超过支架 %s 当前承重：承重 %s 人，本次排了 %d 人，请减少人数或更换支架",
                    bracket.getBracketCode(), capacity.stripTrailingZeros().toPlainString(), expectedPersonCount));
        }
    }

    private SessionDTO toDTO(TrainingSession session, DockingBracket bracket, RowingGroup group) {
        return SessionDTO.from(
                session,
                bracket != null ? bracket.getBracketCode() : null,
                bracket != null ? bracket.getLoadCapacity() : null,
                bracket != null ? bracket.getStatus() : null,
                group != null ? group.getGroupName() : null,
                group != null ? group.getGroupCode() : null);
    }
}
