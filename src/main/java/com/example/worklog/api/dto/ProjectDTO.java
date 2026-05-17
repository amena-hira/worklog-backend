package com.example.worklog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object (DTO) for the Project entity.
 * Used to transfer project data between the client (frontend) and the server (backend)
 * without exposing the raw database entity structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String color;
    private LocalDate dueDate;
    private LocalDate created;
    private LocalDate modified;
    private LocalDate completed;
    
    // We expect the frontend to send the creator's email extracted from their JWT token
    private String createdByUserEmail;

    // List of users assigned to this project and their roles
    private List<ProjectUserDTO> members = new ArrayList<>();

    // Calculated progress fields for the frontend
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer tasksDue;
    private Integer progress; // Percentage from 0 to 100
}
