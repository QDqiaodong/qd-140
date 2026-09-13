package com.rowing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "binding_change_log", indexes = {
        @Index(name = "idx_bracket_id", columnList = "bracket_id"),
        @Index(name = "idx_group_id", columnList = "group_id"),
        @Index(name = "idx_change_type", columnList = "change_type"),
        @Index(name = "idx_changed_at", columnList = "changed_at")
})
public class BindingChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bracket_id", nullable = false)
    private Long bracketId;

    @Column(name = "bracket_code", length = 50)
    private String bracketCode;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType;

    @Column(name = "previous_distance")
    private Integer previousDistance;

    @Column(name = "new_distance")
    private Integer newDistance;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "changed_at")
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();
}