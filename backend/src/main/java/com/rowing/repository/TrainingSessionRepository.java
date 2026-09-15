package com.rowing.repository;

import com.rowing.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    List<TrainingSession> findBySessionDateBetweenOrderBySessionDateAscIdAsc(LocalDate startDate, LocalDate endDate);

    List<TrainingSession> findByBracketIdOrderBySessionDateAscIdAsc(Long bracketId);
}
