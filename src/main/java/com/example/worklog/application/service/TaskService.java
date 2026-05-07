package com.example.worklog.application.service;

import com.example.worklog.api.dto.TaskDTO;
import com.example.worklog.api.dto.TaskUserDTO;
import com.example.worklog.api.mapper.TaskMapper;
import com.example.worklog.infrastructure.persistence.entity.ProjectEntity;
import com.example.worklog.infrastructure.persistence.entity.TaskEntity;
import com.example.worklog.infrastructure.persistence.entity.TaskUserEntity;
import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import com.example.worklog.infrastructure.persistence.repository.ProjectRepository;
import com.example.worklog.infrastructure.persistence.repository.TaskRepository;
import com.example.worklog.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + taskDTO.getProjectId()));
            taskEntity.setProject(project);
        } else {
            throw new RuntimeException("A task must belong to a project. projectId is required.");
        }

        // 3. Link Creator
        if (taskDTO.getCreatedByUserEmail() != null) {
            UserEntity creator = userRepository.findByEmail(taskDTO.getCreatedByUserEmail())
                    .orElseThrow(() -> new RuntimeException("Creator user not found with email: " + taskDTO.getCreatedByUserEmail()));
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
        return taskRepository.findAll().stream()
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
                .orElseThrow(() -> new RuntimeException("Task with id " + id + " not found"));
    }

    /**
     * Retrieves all tasks for a specific project.
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByProjectId(Long projectId) {
        log.info("Fetching tasks for project id: {}", projectId);
        return taskRepository.findByProjectId(projectId).stream()
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
                .orElseThrow(() -> new RuntimeException("Task with id " + id + " not found"));

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
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + taskDTO.getProjectId()));
            existingTask.setProject(project);
        }

        // Update members
        existingTask.getAssignees().clear();
        assignMembersToTask(existingTask, taskDTO.getAssignees());

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
            throw new RuntimeException("Task with id " + id + " not found");
        }
        taskRepository.deleteById(id);
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
                        .orElseThrow(() -> new RuntimeException("User with id " + assigneeDTO.getUserId() + " not found"));
                
                TaskUserEntity taskUser = TaskUserEntity.builder()
                        .task(taskEntity)
                        .user(user)
                        .build();
                taskEntity.getAssignees().add(taskUser);
            }
        }
    }
}
