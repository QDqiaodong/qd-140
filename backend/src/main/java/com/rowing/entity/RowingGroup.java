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
@Table(name = "rowing_group", indexes = {
        @Index(name = "idx_racing_distance", columnList = "racing_distance")
})
public class RowingGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "group_code", unique = true, nullable = false, length = 50)
    private String groupCode;

    @Column(name = "racing_distance", nullable = false)
    private Integer racingDistance;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "plan_file_name", length = 255)
    private String planFileName;

    @Column(name = "plan_file_path", length = 500)
    private String planFilePath;

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