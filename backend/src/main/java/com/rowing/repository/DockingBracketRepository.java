package com.rowing.repository;

import com.rowing.entity.DockingBracket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DockingBracketRepository extends JpaRepository<DockingBracket, Long> {

    Optional<DockingBracket> findByBracketCode(String bracketCode);

    boolean existsByBracketCode(String bracketCode);

    @Query("SELECT b FROM DockingBracket b WHERE b.status = 1 AND b.minDistance <= :distance AND b.maxDistance >= :distance")
    List<DockingBracket> findByDistanceRange(@Param("distance") Integer distance);

    @Query("SELECT b FROM DockingBracket b WHERE " +
           "(:bracketCode IS NULL OR b.bracketCode LIKE %:bracketCode%) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:minDistance IS NULL OR b.minDistance <= :minDistance) AND " +
           "(:maxDistance IS NULL OR b.maxDistance >= :maxDistance)")
    Page<DockingBracket> findByConditions(
            @Param("bracketCode") String bracketCode,
            @Param("minDistance") Integer minDistance,
            @Param("maxDistance") Integer maxDistance,
            @Param("status") Integer status,
            Pageable pageable);

    @Query("SELECT DISTINCT b FROM DockingBracket b WHERE b.status = 1")
    List<DockingBracket> findAllEnabled();
}