package com.example.worklog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing a user's task statistics on the dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskStatsDTO {
    private int totalTasks;
    private int completedTasks;
    private int incompleteTasks;
    private int overdueTasks;
}
