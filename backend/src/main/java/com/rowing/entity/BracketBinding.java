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
@Table(name = "bracket_binding", indexes = {
        @Index(name = "idx_bracket_id", columnList = "bracket_id"),
        @Index(name = "idx_group_id", columnList = "group_id"),
        @Index(name = "idx_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_bracket_group", columnNames = {"bracket_id", "group_id"})
})
public class BracketBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bracket_id", nullable = false)
    private Long bracketId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "binding_time")
    @Builder.Default
    private LocalDateTime bindingTime = LocalDateTime.now();

    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;
}