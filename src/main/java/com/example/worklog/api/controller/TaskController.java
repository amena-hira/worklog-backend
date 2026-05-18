package com.example.worklog.api.controller;

import com.example.worklog.api.dto.TaskDTO;
import com.example.worklog.api.dto.TaskStatusUpdateDTO;
import com.example.worklog.api.dto.UserTaskStatsDTO;
import com.example.worklog.application.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Task entities.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    /**
     * Creates a new task.
     * The creator's email is securely extracted from the JWT token.
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDTO, Authentication authentication) {
        // Inject the email from the secure token into the DTO before passing to the service
        taskDTO.setCreatedByUserEmail(authentication.getName());
        TaskDTO createdTask = taskService.createTask(taskDTO);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProjectId(projectId));
    }

    /**
     * Retrieves all tasks relevant to the currently authenticated user (created by or assigned to).
     */
    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskDTO>> getMyTasks(Authentication authentication) {
        // authentication.getName() returns the username/email of the logged-in user from the JWT
        return ResponseEntity.ok(taskService.getTasksForUser(authentication.getName()));
    }

    /**
     * Retrieves tasks due today that are relevant to the currently authenticated user.
     */
    @GetMapping("/my-tasks/due-today")
    public ResponseEntity<List<TaskDTO>> getMyTasksDueToday(Authentication authentication) {
        return ResponseEntity.ok(taskService.getTasksDueTodayForUser(authentication.getName()));
    }

    /**
     * Retrieves task statistics for the currently authenticated user.
     */
    @GetMapping("/stats")
    public ResponseEntity<UserTaskStatsDTO> getMyTaskStats(Authentication authentication) {
        return ResponseEntity.ok(taskService.getUserTaskStats(authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDTO));
    }

    /**
     * Updates only the completion status of a specific task.
     * Uses PATCH as it's a partial update of the resource.
     *
     * @param id The ID of the task.
     * @param statusDTO The lightweight DTO containing just the new status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(@PathVariable Long id, @RequestBody TaskStatusUpdateDTO statusDTO) {
        log.info("Updating completion status for task id: {} {} {}", id, statusDTO,statusDTO.getIsCompleted());
        return ResponseEntity.ok(taskService.updateTaskStatus(id, statusDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
