package com.example.worklog.application.service;

import com.example.worklog.api.dto.TaskDTO;
import com.example.worklog.api.dto.TaskStatusUpdateDTO;
import com.example.worklog.api.dto.TaskUserDTO;
import com.example.worklog.api.dto.UserTaskStatsDTO;
import com.example.worklog.api.mapper.TaskMapper;
import com.example.worklog.exception.InvalidOperationException;
import com.example.worklog.exception.ResourceNotFoundException;
import com.example.worklog.infrastructure.persistence.entity.ProjectEntity;
import com.example.worklog.infrastructure.persistence.entity.TaskEntity;
import com.example.worklog.infrastructure.persistence.entity.TaskUserEntity;
import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import com.example.worklog.infrastructure.persistence.repository.ProjectRepository;
import com.example.worklog.infrastructure.persistence.repository.TaskRepository;
import com.example.worklog.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.worklog.exception.ResourceInUseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new task and associates it with a project, creator, and assignees.
     */
    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        log.info("Creating new task: {}", taskDTO.getName());
        
        TaskEntity taskEntity = taskMapper.toEntity(taskDTO);
        
        // 1. Set default values
        taskEntity.setCreated(LocalDate.now());
        taskEntity.setModified(LocalDate.now());
        if (taskEntity.getIsCompleted() == null) {
            taskEntity.setIsCompleted(false);
        }

        // 2. Link Project
        if (taskDTO.getProjectId() != null) {
            ProjectEntity project = projectRepository.findById(taskDTO.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + taskDTO.getProjectId()));
            taskEntity.setProject(project);
        } else {
            throw new InvalidOperationException("A task must belong to a project. projectId is required.");
        }

        // 3. Link Creator
        if (taskDTO.getCreatedByUserEmail() != null) {
            UserEntity creator = userRepository.findByEmail(taskDTO.getCreatedByUserEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Creator user not found with email: " + taskDTO.getCreatedByUserEmail()));
            taskEntity.setCreatedBy(creator);
        }

        // 4. Assign Users
        assignMembersToTask(taskEntity, taskDTO.getAssignees());

        TaskEntity savedTask = taskRepository.save(taskEntity);
        return taskMapper.toDTO(savedTask);
    }

    /**
     * Retrieves all tasks.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        log.info("Fetching all tasks");
        return taskRepository.findAllByOrderByCreatedDesc().stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific task by its ID.
     */
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        log.info("Fetching task with id: {}", id);
        return taskRepository.findById(id)
                .map(taskMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }

    /**
     * Retrieves all tasks for a specific project.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByProjectId(Long projectId) {
        log.info("Fetching tasks for project id: {}", projectId);
        return taskRepository.findByProjectIdOrderByCreatedDesc(projectId).stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all tasks where the user is either the creator or an assignee.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksForUser(String userEmail) {
        log.info("Fetching all tasks for user email: {}", userEmail);
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
                
        return taskRepository.findTasksByUserInvolvement(user.getId()).stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves tasks due today where the user is either the creator or an assignee.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksDueTodayForUser(String userEmail) {
        log.info("Fetching tasks due today for user email: {}", userEmail);
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
                
        return taskRepository.findTasksDueTodayByUserInvolvement(user.getId(), LocalDate.now()).stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing task and its assignees.
     */
    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        log.info("Updating task with id: {}", id);
        
        TaskEntity existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        // Update basic fields
        existingTask.setName(taskDTO.getName());
        existingTask.setDescription(taskDTO.getDescription());
        existingTask.setPriority(taskDTO.getPriority());
        existingTask.setDueDate(taskDTO.getDueDate());
        
        // Handle completion logic
        if (taskDTO.getIsCompleted() != null && taskDTO.getIsCompleted() && !Boolean.TRUE.equals(existingTask.getIsCompleted())) {
            existingTask.setCompleted(LocalDate.now());
        } else if (taskDTO.getIsCompleted() != null && !taskDTO.getIsCompleted()) {
            existingTask.setCompleted(null);
        }
        existingTask.setIsCompleted(taskDTO.getIsCompleted());
        
        existingTask.setModified(LocalDate.now());

        // Update Project if changed (optional business logic, often tasks stay in same project)
        if (taskDTO.getProjectId() != null && !taskDTO.getProjectId().equals(existingTask.getProject().getId())) {
            ProjectEntity project = projectRepository.findById(taskDTO.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + taskDTO.getProjectId()));
            existingTask.setProject(project);
        }

        // Update members
        existingTask.getAssignees().clear();
        assignMembersToTask(existingTask, taskDTO.getAssignees());

        TaskEntity updatedTask = taskRepository.save(existingTask);
        return taskMapper.toDTO(updatedTask);
    }

    /**
     * Updates only the completion status of a task.
     */
    @Transactional
    public TaskDTO updateTaskStatus(Long id, TaskStatusUpdateDTO statusDTO) {
        log.info("Updating completion status for task id: {}", id);
        
        TaskEntity existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        if (statusDTO.getIsCompleted() != null) {
            if (statusDTO.getIsCompleted() && !Boolean.TRUE.equals(existingTask.getIsCompleted())) {
                existingTask.setCompleted(LocalDate.now());
            } else if (!statusDTO.getIsCompleted()) {
                existingTask.setCompleted(null);
            }
            existingTask.setIsCompleted(statusDTO.getIsCompleted());
            existingTask.setModified(LocalDate.now());
        }

        TaskEntity updatedTask = taskRepository.save(existingTask);
        return taskMapper.toDTO(updatedTask);
    }

    /**
     * Deletes a task.
     */
    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task with id " + id + " not found");
        }
        try {
            taskRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceInUseException("Cannot delete task. It is still being referenced.");
        }
    }

    /**
     * Calculates task statistics for a specific user, considering both tasks they created and tasks assigned to them.
     * @param userEmail The email of the user to fetch stats for.
     * @return UserTaskStatsDTO containing total, completed, incomplete, and overdue counts.
     */
    @Transactional(readOnly = true)
    public UserTaskStatsDTO getUserTaskStats(String userEmail) {
        log.info("Calculating task statistics for user email: {}", userEmail);

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Long userId = user.getId();

        int total = taskRepository.countTasksByUserInvolvement(userId);
        int completed = taskRepository.countTasksByUserInvolvementAndIsCompleted(userId, true);
        int incomplete = taskRepository.countTasksByUserInvolvementAndIsCompleted(userId, false);
        int overdue = taskRepository.countOverdueTasksByUserInvolvement(userId, LocalDate.now());

        return UserTaskStatsDTO.builder()
                .totalTasks(total)
                .completedTasks(completed)
                .incompleteTasks(incomplete)
                .overdueTasks(overdue)
                .build();
    }

    /**
     * Helper method to map a list of TaskUserDTOs to TaskUserEntities and add them to a task.
     */
    private void assignMembersToTask(TaskEntity taskEntity, List<TaskUserDTO> assigneeDTOs) {
        if (assigneeDTOs != null && !assigneeDTOs.isEmpty()) {
            if (taskEntity.getAssignees() == null) {
                taskEntity.setAssignees(new ArrayList<>());
            }
            
            for (TaskUserDTO assigneeDTO : assigneeDTOs) {
                UserEntity user = userRepository.findById(assigneeDTO.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User with id " + assigneeDTO.getUserId() + " not found"));
                
                TaskUserEntity taskUser = TaskUserEntity.builder()
                        .task(taskEntity)
                        .user(user)
                        .build();
                taskEntity.getAssignees().add(taskUser);
            }
        }
    }
}
