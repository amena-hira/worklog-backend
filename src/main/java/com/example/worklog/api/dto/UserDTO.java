package com.example.worklog.api.dto;

import lombok.*;

/**
 * Standard Data Transfer Object for User responses.
 * Does not expose the internal database ID.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String first_name;
    private String last_name;
    private String email;
    private String password; // Used for creation/updates, typically null/ignored in responses
    private String gender;
    private String role;
}
