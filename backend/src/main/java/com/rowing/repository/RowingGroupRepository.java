package com.rowing.repository;

import com.rowing.entity.RowingGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RowingGroupRepository extends JpaRepository<RowingGroup, Long> {

    Optional<RowingGroup> findByGroupCode(String groupCode);

    boolean existsByGroupCode(String groupCode);

    @Query("SELECT g FROM RowingGroup g WHERE g.racingDistance = :distance")
    List<RowingGroup> findByRacingDistance(@Param("distance") Integer distance);

    @Query("SELECT DISTINCT g.racingDistance FROM RowingGroup g ORDER BY g.racingDistance")
    List<Integer> findDistinctRacingDistances();

    @Query("SELECT g FROM RowingGroup g WHERE " +
           "(:groupName IS NULL OR g.groupName LIKE %:groupName%) AND " +
           "(:groupCode IS NULL OR g.groupCode LIKE %:groupCode%) AND " +
           "(:racingDistance IS NULL OR g.racingDistance = :racingDistance)")
    Page<RowingGroup> findByConditions(
            @Param("groupName") String groupName,
            @Param("groupCode") String groupCode,
            @Param("racingDistance") Integer racingDistance,
            Pageable pageable);
}