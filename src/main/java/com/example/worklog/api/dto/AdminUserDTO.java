package com.example.worklog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object specifically for Admin responses.
 * Includes sensitive or administrative fields like the user ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {
    private Long id;
    private String first_name;
    private String last_name;
    private String email;
    private String gender;
    private String role;
}
