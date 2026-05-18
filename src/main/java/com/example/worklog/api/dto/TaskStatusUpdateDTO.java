package com.example.worklog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A lightweight DTO used specifically for updating only the completion status of a task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusUpdateDTO {
    private Boolean isCompleted;
}
