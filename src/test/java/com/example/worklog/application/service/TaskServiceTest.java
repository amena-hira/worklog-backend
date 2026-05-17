package com.example.worklog.application.service;

import com.example.worklog.api.dto.TaskDTO;
import com.example.worklog.api.mapper.TaskMapper;
import com.example.worklog.infrastructure.persistence.entity.ProjectEntity;
import com.example.worklog.infrastructure.persistence.entity.TaskEntity;
import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import com.example.worklog.infrastructure.persistence.repository.ProjectRepository;
import com.example.worklog.infrastructure.persistence.repository.TaskRepository;
import com.example.worklog.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private TaskDTO taskDTO;
    private TaskEntity taskEntity;
    private ProjectEntity projectEntity;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .id(1L)
                .email("test@mail.com")
                .build();

        projectEntity = ProjectEntity.builder()
                .id(1L)
                .name("Test Project")
                .build();

        taskDTO = TaskDTO.builder()
                .id(1L)
                .name("Test Task")
                .projectId(1L)
                .createdByUserEmail("test@mail.com")
                .build();

        taskEntity = TaskEntity.builder()
                .id(1L)
                .name("Test Task")
                .project(projectEntity)
                .createdBy(userEntity)
                .build();
    }

    @Test
    void createTask_Success() {
        // Arrange
        when(taskMapper.toEntity(taskDTO)).thenReturn(taskEntity);
        when(projectRepository.findById(taskDTO.getProjectId())).thenReturn(Optional.of(projectEntity));
        when(userRepository.findByEmail(taskDTO.getCreatedByUserEmail())).thenReturn(Optional.of(userEntity));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(taskEntity);
        when(taskMapper.toDTO(taskEntity)).thenReturn(taskDTO);

        // Act
        TaskDTO result = taskService.createTask(taskDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Test Task", result.getName());
        verify(projectRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("test@mail.com");
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }

    @Test
    void createTask_ProjectNotFound_ThrowsException() {
        // Arrange
        when(taskMapper.toEntity(taskDTO)).thenReturn(taskEntity);
        when(projectRepository.findById(taskDTO.getProjectId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.createTask(taskDTO);
        });

        assertTrue(exception.getMessage().contains("Project not found"));
        verify(userRepository, never()).findByEmail(any());
        verify(taskRepository, never()).save(any());
    }
}
