package com.example.worklog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing a user's membership and role within a project.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserDTO {
    private Long userId; // The ID is required when the frontend sends data to assign the user
    private String userName; // Combined first_name + last_name
    private String userEmail; 
    private String role;
}
