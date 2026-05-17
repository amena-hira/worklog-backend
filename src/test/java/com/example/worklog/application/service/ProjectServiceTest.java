package com.example.worklog.application.service;

import com.example.worklog.api.dto.ProjectDTO;
import com.example.worklog.api.mapper.ProjectMapper;
import com.example.worklog.infrastructure.persistence.entity.ProjectEntity;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository; // Added this mock

    @InjectMocks
    private ProjectService projectService;

    private ProjectDTO projectDTO;
    private ProjectEntity projectEntity;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        // Initialize test data
        projectDTO = ProjectDTO.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .createdByUserEmail("test@mail.com")
                .build();

        userEntity = UserEntity.builder()
                .id(1L)
                .email("test@mail.com")
                .first_name("Test")
                .last_name("User")
                .build();

        projectEntity = ProjectEntity.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .createdBy(userEntity)
                .build();
    }

    @Test
    void createProject_Success() {
        // Arrange
        when(projectMapper.toEntity(projectDTO)).thenReturn(projectEntity);
        when(userRepository.findByEmail(projectDTO.getCreatedByUserEmail())).thenReturn(Optional.of(userEntity));
        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(projectEntity);
        when(projectMapper.toDTO(projectEntity)).thenReturn(projectDTO);
        // Stub the new calls for progress calculation
        when(taskRepository.countByProjectId(anyLong())).thenReturn(0);
        when(taskRepository.countByProjectIdAndIsCompleted(anyLong(), anyBoolean())).thenReturn(0);

        // Act
        ProjectDTO result = projectService.createProject(projectDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        verify(userRepository, times(1)).findByEmail("test@mail.com");
        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
    }

    @Test
    void createProject_UserNotFound_ThrowsException() {
        // Arrange
        when(projectMapper.toEntity(projectDTO)).thenReturn(projectEntity);
        when(userRepository.findByEmail(projectDTO.getCreatedByUserEmail())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectService.createProject(projectDTO);
        });

        assertTrue(exception.getMessage().contains("Creator user not found"));
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void getProjectById_Success() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectEntity));
        when(projectMapper.toDTO(projectEntity)).thenReturn(projectDTO);
        // Stub the new calls for progress calculation
        when(taskRepository.countByProjectId(anyLong())).thenReturn(10);
        when(taskRepository.countByProjectIdAndIsCompleted(anyLong(), eq(true))).thenReturn(5);

        // Act
        ProjectDTO result = projectService.getProjectById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(50, result.getProgress()); // Check if progress is calculated correctly
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    void getProjectById_NotFound_ThrowsException() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            projectService.getProjectById(1L);
        });
        verify(projectMapper, never()).toDTO(any());
    }

    @Test
    void deleteProject_Success() {
        // Arrange
        when(projectRepository.existsById(1L)).thenReturn(true);

        // Act
        projectService.deleteProject(1L);

        // Assert
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProject_NotFound_ThrowsException() {
        // Arrange
        when(projectRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            projectService.deleteProject(1L);
        });
        verify(projectRepository, never()).deleteById(any());
    }
}
