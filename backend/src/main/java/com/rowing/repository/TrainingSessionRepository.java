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

    /** 查某支架上仍然有效、且上课日还没到或就是今天的课次（停用时批量标停用用；已过上课日的课不动） */
    List<TrainingSession> findByBracketIdAndStatusAndSessionDateGreaterThanEqual(Long bracketId, Integer status, LocalDate sessionDate);
}
