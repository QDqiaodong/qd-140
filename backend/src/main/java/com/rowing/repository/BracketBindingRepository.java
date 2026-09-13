package com.rowing.repository;

import com.rowing.entity.BracketBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BracketBindingRepository extends JpaRepository<BracketBinding, Long> {

    Optional<BracketBinding> findByBracketIdAndGroupId(Long bracketId, Long groupId);

    List<BracketBinding> findByBracketId(Long bracketId);

    List<BracketBinding> findByGroupId(Long groupId);

    @Query("SELECT b FROM BracketBinding b WHERE b.status = 1")
    List<BracketBinding> findAllActive();

    @Query("SELECT COUNT(b) FROM BracketBinding b WHERE b.status = 1 AND b.bracketId IN " +
           "(SELECT br.id FROM DockingBracket br WHERE br.status = 1 AND br.minDistance <= :distance AND br.maxDistance >= :distance)")
    Long countByDistanceRange(@Param("distance") Integer distance);

    @Modifying
    @Query("UPDATE BracketBinding b SET b.status = 0 WHERE b.groupId = :groupId AND b.status = 1")
    void deactivateByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Query("UPDATE BracketBinding b SET b.status = 0 WHERE b.bracketId = :bracketId AND b.status = 1")
    void deactivateByBracketId(@Param("bracketId") Long bracketId);
}