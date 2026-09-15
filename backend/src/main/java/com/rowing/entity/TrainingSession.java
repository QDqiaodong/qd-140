package com.rowing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "training_session", indexes = {
        @Index(name = "idx_session_date", columnList = "session_date"),
        @Index(name = "idx_bracket_id", columnList = "bracket_id"),
        @Index(name = "idx_group_id", columnList = "group_id")
})
public class TrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "bracket_id", nullable = false)
    private Long bracketId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "expected_person_count", nullable = false)
    private Integer expectedPersonCount;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
