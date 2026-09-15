package com.rowing.service.impl;

import com.rowing.dto.request.SessionCreateRequest;
import com.rowing.dto.request.SessionUpdateRequest;
import com.rowing.dto.response.SessionDTO;
import com.rowing.entity.DockingBracket;
import com.rowing.entity.TrainingSession;
import com.rowing.exception.BusinessException;
import com.rowing.repository.DockingBracketRepository;
import com.rowing.repository.RowingGroupRepository;
import com.rowing.repository.TrainingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionServiceImplTest {

    @Mock
    private TrainingSessionRepository sessionRepository;
    @Mock
    private DockingBracketRepository bracketRepository;
    @Mock
    private RowingGroupRepository groupRepository;

    @InjectMocks
    private TrainingSessionServiceImpl service;

    private DockingBracket bracket;

    @BeforeEach
    void setUp() {
        bracket = DockingBracket.builder()
                .id(1L)
                .bracketCode("BK-001")
                .loadCapacity(new BigDecimal("150"))
                .minDistance(500)
                .maxDistance(2000)
                .status(1)
                .build();
        when(bracketRepository.findById(1L)).thenReturn(Optional.of(bracket));
        lenient().when(sessionRepository.save(any(TrainingSession.class)))
                .thenAnswer(inv -> {
                    TrainingSession s = inv.getArgument(0);
                    s.setId(100L);
                    return s;
                });
    }

    private SessionCreateRequest createReq(Integer people) {
        return SessionCreateRequest.builder()
                .sessionDate(LocalDate.of(2026, 9, 16))
                .bracketId(1L)
                .groupId(null)
                .expectedPersonCount(people)
                .remark(null)
                .build();
    }

    @Test
    void create_okWhenAtCapacity() {
        SessionDTO dto = service.create(createReq(150));
        assertThat(dto.getValid()).isTrue();
        assertThat(dto.getOverloaded()).isFalse();
        assertThat(dto.getExpectedPersonCount()).isEqualTo(150);
    }

    @Test
    void create_blockedWhenOverCapacity_messageCarriesCapacityAndPeople() {
        assertThatThrownBy(() -> service.create(createReq(151)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("承重 150 人")
                .hasMessageContaining("本次排了 151 人");
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void create_blockedWhenPeopleNull() {
        assertThatThrownBy(() -> service.create(createReq(null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("人数不能为空");
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void create_blockedWhenPeopleZeroOrNegative() {
        assertThatThrownBy(() -> service.create(createReq(0)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("必须大于0");
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void oldSessionBecomeOverloadedAfterCapacityReduced_andUpdateBlockedByNewCapacity() {
        // 已排好：150 人，旧承重 150
        TrainingSession existing = TrainingSession.builder()
                .id(100L)
                .sessionDate(LocalDate.of(2026, 9, 16))
                .bracketId(1L)
                .groupId(null)
                .expectedPersonCount(150)
                .build();

        // 场务把承重下调到 120
        bracket.setLoadCapacity(new BigDecimal("120"));

        // 查询时实时判定 -> 超载、无效
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(existing));
        SessionDTO view = service.getById(100L);
        assertThat(view.getOverloaded()).isTrue();
        assertThat(view.getValid()).isFalse();

        // 不改人数（仍是150）直接保存 -> 按新承重拦住
        SessionUpdateRequest req = SessionUpdateRequest.builder()
                .id(100L)
                .sessionDate(LocalDate.of(2026, 9, 16))
                .bracketId(1L)
                .groupId(null)
                .expectedPersonCount(150)
                .remark(null)
                .build();
        assertThatThrownBy(() -> service.update(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("承重 120 人")
                .hasMessageContaining("本次排了 150 人");

        // 把人数降到新承重以内 -> 可保存，且不再超载
        req.setExpectedPersonCount(120);
        SessionDTO saved = service.update(req);
        assertThat(saved.getValid()).isTrue();
        assertThat(saved.getOverloaded()).isFalse();

        ArgumentCaptor<TrainingSession> captor = ArgumentCaptor.forClass(TrainingSession.class);
        verify(sessionRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getExpectedPersonCount()).isEqualTo(120);
    }

    @Test
    void nonOverloadedSessionNotAffectedByUnrelatedChange() {
        TrainingSession existing = TrainingSession.builder()
                .id(101L)
                .sessionDate(LocalDate.of(2026, 9, 17))
                .bracketId(1L)
                .expectedPersonCount(100)
                .build();
        // 承重仍为150，100人的课不受影响
        when(sessionRepository.findById(101L)).thenReturn(Optional.of(existing));
        SessionDTO view = service.getById(101L);
        assertThat(view.getOverloaded()).isFalse();
        assertThat(view.getValid()).isTrue();
    }

    @Test
    void markedDisabledSession_staysInvalidEvenAfterBracketReenabled() {
        // 支架停用时这节课被标掉（status=0）；之后支架重新启用，课不能自动变回有效
        TrainingSession marked = TrainingSession.builder()
                .id(200L)
                .sessionDate(LocalDate.now().plusDays(1))
                .bracketId(1L)
                .expectedPersonCount(100)
                .status(0)
                .build();
        when(sessionRepository.findById(200L)).thenReturn(Optional.of(marked));

        SessionDTO view = service.getById(200L);
        assertThat(view.getStatus()).isEqualTo(0);
        assertThat(view.getValid()).isFalse();
        assertThat(view.getBracketDisabled()).isTrue();
    }

    @Test
    void upcomingSession_onCurrentlyDisabledBracket_showsNotAttendable() {
        // 关掉排课再打开：支架仍处于停用，未上课次依然能看出来不能上，且原因是支架停用
        bracket.setStatus(0);
        TrainingSession upcoming = TrainingSession.builder()
                .id(201L)
                .sessionDate(LocalDate.now().plusDays(1))
                .bracketId(1L)
                .expectedPersonCount(100)
                .status(1)
                .build();
        when(sessionRepository.findById(201L)).thenReturn(Optional.of(upcoming));

        SessionDTO view = service.getById(201L);
        assertThat(view.getValid()).isFalse();
        assertThat(view.getBracketDisabled()).isTrue();
    }

    @Test
    void pastSession_onDisabledBracket_keepsHistoricalRecord() {
        // 已过上课日的课不回溯标停用，留着当时的记录
        bracket.setStatus(0);
        TrainingSession past = TrainingSession.builder()
                .id(202L)
                .sessionDate(LocalDate.now().minusDays(1))
                .bracketId(1L)
                .expectedPersonCount(100)
                .status(1)
                .build();
        when(sessionRepository.findById(202L)).thenReturn(Optional.of(past));

        SessionDTO view = service.getById(202L);
        assertThat(view.getBracketDisabled()).isFalse();
        assertThat(view.getValid()).isTrue();
    }

    @Test
    void rescheduleMarkedSession_viaUpdate_restoresValid() {
        // 因支架停用被标掉的课，场务改课重排（支架已重新启用、人数不超载）后恢复有效
        TrainingSession marked = TrainingSession.builder()
                .id(203L)
                .sessionDate(LocalDate.now().plusDays(2))
                .bracketId(1L)
                .expectedPersonCount(100)
                .status(0)
                .build();
        when(sessionRepository.findById(203L)).thenReturn(Optional.of(marked));

        SessionUpdateRequest req = SessionUpdateRequest.builder()
                .id(203L)
                .sessionDate(LocalDate.now().plusDays(2))
                .bracketId(1L)
                .groupId(null)
                .expectedPersonCount(100)
                .remark(null)
                .build();
        SessionDTO saved = service.update(req);

        assertThat(marked.getStatus()).isEqualTo(1);
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getValid()).isTrue();
        assertThat(saved.getBracketDisabled()).isFalse();
    }

    @Test
    void create_blockedWhenBracketDisabled() {
        // 停用保存成功后，不能再往这根支架上排新课
        bracket.setStatus(0);
        assertThatThrownBy(() -> service.create(createReq(100)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已禁用");
    }
}
