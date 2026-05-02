package com.example.worklog.api.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String first_name;
    private String last_name;
    private String email;
    private String password;
    private String gender;
    private String role;
}
