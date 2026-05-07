package com.example.worklog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object (DTO) for the Task entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String name;
    private String description;
    private String priority;
    private Boolean isCompleted;
    private LocalDate dueDate;
    private LocalDate created;
    private LocalDate modified;
    private LocalDate completed;
    
    // The ID of the project this task belongs to
    private Long projectId;

    // The email of the user who created the task (from JWT usually)
    private String createdByUserEmail;

    // List of users assigned to this task
    private List<TaskUserDTO> assignees = new ArrayList<>();
}
