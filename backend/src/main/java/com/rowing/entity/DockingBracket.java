package com.rowing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "docking_bracket", indexes = {
        @Index(name = "idx_distance_range", columnList = "min_distance, max_distance"),
        @Index(name = "idx_status", columnList = "status")
})
public class DockingBracket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bracket_code", unique = true, nullable = false, length = 50)
    private String bracketCode;

    @Column(name = "load_capacity", nullable = false, precision = 10, scale = 2)
    private BigDecimal loadCapacity;

    @Column(name = "min_distance", nullable = false)
    private Integer minDistance;

    @Column(name = "max_distance", nullable = false)
    private Integer maxDistance;

    @Column(name = "status")
    @Builder.Default
    private Integer status = 1;

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