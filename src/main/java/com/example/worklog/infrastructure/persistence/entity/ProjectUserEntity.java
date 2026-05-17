package com.example.worklog.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project_users")
public class ProjectUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which project
    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    // Which user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // User role in this project
    private String role; // OWNER, DEVELOPER, TESTER, DESIGNER
}
