package com.rowing.service.impl;

import com.rowing.dto.request.BracketUpdateRequest;
import com.rowing.entity.DockingBracket;
import com.rowing.entity.TrainingSession;
import com.rowing.repository.BracketBindingRepository;
import com.rowing.repository.DockingBracketRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DockingBracketServiceImplTest {

    @Mock
    private DockingBracketRepository bracketRepository;
    @Mock
    private BracketBindingRepository bindingRepository;
    @Mock
    private TrainingSessionRepository sessionRepository;

    @InjectMocks
    private DockingBracketServiceImpl service;

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
        when(bracketRepository.save(any(DockingBracket.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void disable_marksTodayAndFutureSessionsInvalid_andLeavesPastOnesAlone() {
        // 停用保存成功后：上课日就是今天、还没到上课日的有效课次都要被标掉
        TrainingSession todaySession = TrainingSession.builder()
                .id(101L).sessionDate(LocalDate.now()).bracketId(1L).expectedPersonCount(100).status(1).build();
        TrainingSession futureSession = TrainingSession.builder()
                .id(102L).sessionDate(LocalDate.now().plusDays(3)).bracketId(1L).expectedPersonCount(100).status(1).build();
        // 只查“仍有效 + 上课日 >= 今天”的课次：已过上课日的课不在范围内，留着当时的记录
        when(sessionRepository.findByBracketIdAndStatusAndSessionDateGreaterThanEqual(1L, 1, LocalDate.now()))
                .thenReturn(List.of(todaySession, futureSession));

        service.update(BracketUpdateRequest.builder().id(1L).status(0).build());

        assertThat(bracket.getStatus()).isEqualTo(0);
        assertThat(todaySession.getStatus()).isEqualTo(0);
        assertThat(futureSession.getStatus()).isEqualTo(0);
        verify(sessionRepository)
                .findByBracketIdAndStatusAndSessionDateGreaterThanEqual(1L, 1, LocalDate.now());
        ArgumentCaptor<List<TrainingSession>> captor = ArgumentCaptor.forClass(List.class);
        verify(sessionRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2).allMatch(s -> s.getStatus() == 0);
    }

    @Test
    void enableAgain_doesNotTouchSessions_markedOnesStayInvalid() {
        // 重新启用只恢复支架状态，绝不动课次：因停用标掉的课不会自动变回有效，须场务自己重排
        service.update(BracketUpdateRequest.builder().id(1L).status(1).build());

        assertThat(bracket.getStatus()).isEqualTo(1);
        verifyNoInteractions(sessionRepository);
    }

    @Test
    void disable_whenNoUpcomingValidSessions_onlyUpdatesBracket() {
        when(sessionRepository.findByBracketIdAndStatusAndSessionDateGreaterThanEqual(1L, 1, LocalDate.now()))
                .thenReturn(List.of());

        service.update(BracketUpdateRequest.builder().id(1L).status(0).build());

        assertThat(bracket.getStatus()).isEqualTo(0);
        verify(sessionRepository, never()).saveAll(any());
    }

    @Test
    void delete_softDeleteAlso_marksUpcomingSessionsInvalid() {
        // 删除（软删）视同停用：未上课次一并标掉
        TrainingSession futureSession = TrainingSession.builder()
                .id(103L).sessionDate(LocalDate.now().plusDays(1)).bracketId(1L).expectedPersonCount(100).status(1).build();
        when(sessionRepository.findByBracketIdAndStatusAndSessionDateGreaterThanEqual(1L, 1, LocalDate.now()))
                .thenReturn(List.of(futureSession));

        service.delete(1L);

        assertThat(bracket.getStatus()).isEqualTo(0);
        assertThat(futureSession.getStatus()).isEqualTo(0);
        verify(bindingRepository).deactivateByBracketId(1L);
    }
}
