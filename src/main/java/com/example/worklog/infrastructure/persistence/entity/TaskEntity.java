package com.example.worklog.infrastructure.persistence.entity;

import java.time.LocalDate;
import java.util.*;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String priority;
    private Boolean isCompleted;
    private LocalDate dueDate;
    private LocalDate created;
    private LocalDate modified;
    private LocalDate completed;

    // Task belongs to one project
    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    // Task creator / owner
    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private UserEntity createdBy;

    // Task assigned members
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<TaskUserEntity> assignees = new ArrayList<>();
}
