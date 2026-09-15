package com.rowing.repository;

import com.rowing.entity.BindingChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BindingChangeLogRepository extends JpaRepository<BindingChangeLog, Long> {

    List<BindingChangeLog> findByBracketId(Long bracketId);

    List<BindingChangeLog> findByGroupId(Long groupId);

    @Query("SELECT l FROM BindingChangeLog l WHERE " +
           "(:bracketCode IS NULL OR l.bracketCode LIKE %:bracketCode%) AND " +
           "(:groupName IS NULL OR l.groupName LIKE %:groupName%) AND " +
           "(:changeType IS NULL OR l.changeType = :changeType)")
    Page<BindingChangeLog> findByConditions(
            @Param("bracketCode") String bracketCode,
            @Param("groupName") String groupName,
            @Param("changeType") String changeType,
            Pageable pageable);

    @Query("SELECT l FROM BindingChangeLog l WHERE " +
           "(:bracketCode IS NULL OR l.bracketCode LIKE %:bracketCode%) AND " +
           "(:groupName IS NULL OR l.groupName LIKE %:groupName%) AND " +
           "(:changeType IS NULL OR l.changeType = :changeType)")
    List<BindingChangeLog> findAllByConditions(
            @Param("bracketCode") String bracketCode,
            @Param("groupName") String groupName,
            @Param("changeType") String changeType,
            Sort sort);

    @Query("SELECT l FROM BindingChangeLog l ORDER BY l.changedAt DESC LIMIT :limit")
    List<BindingChangeLog> findRecentLogs(@Param("limit") Integer limit);
}