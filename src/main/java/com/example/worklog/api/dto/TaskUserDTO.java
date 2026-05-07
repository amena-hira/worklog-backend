package com.example.worklog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing a user assigned to a task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskUserDTO {
    private Long userId; // The ID is required when assigning a user
    private String userName; // Combined first_name + last_name
    private String userEmail; 
}
