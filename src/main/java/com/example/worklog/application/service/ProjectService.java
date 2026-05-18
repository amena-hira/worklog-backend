package com.example.worklog.application.service;

import com.example.worklog.api.dto.ProjectDTO;
import com.example.worklog.api.dto.ProjectUserDTO;
import com.example.worklog.api.mapper.ProjectMapper;
import com.example.worklog.exception.ResourceInUseException;
import com.example.worklog.exception.ResourceNotFoundException;
import com.example.worklog.infrastructure.persistence.entity.ProjectEntity;
import com.example.worklog.infrastructure.persistence.entity.ProjectUserEntity;
import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import com.example.worklog.infrastructure.persistence.repository.ProjectRepository;
import com.example.worklog.infrastructure.persistence.repository.TaskRepository;
import com.example.worklog.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    /**
     * Creates a new project in the database.
     * Sets the creation/modification dates, links the user who created it based on their email,
     * and assigns any initial members provided.
     *
     * @param projectDTO The project data from the client.
     * @return The created project as a DTO.
     */
    @Transactional
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        log.info("Creating new project: {}", projectDTO.getName());
        
        ProjectEntity projectEntity = projectMapper.toEntity(projectDTO);
        
        // Automatically set tracking dates
        projectEntity.setCreated(LocalDate.now());
        projectEntity.setModified(LocalDate.now());

        // If a creator email is provided (typically from the frontend parsing the JWT), fetch the User
        if (projectDTO.getCreatedByUserEmail() != null) {
            UserEntity creator = userRepository.findByEmail(projectDTO.getCreatedByUserEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Creator user not found with email: " + projectDTO.getCreatedByUserEmail()));
            projectEntity.setCreatedBy(creator);
        }
        
        // Handle assigning members to the project
        assignMembersToProject(projectEntity, projectDTO.getMembers());

        ProjectEntity savedProject = projectRepository.save(projectEntity);
        return calculateProgressForProject(projectMapper.toDTO(savedProject));
    }

    /**
     * Retrieves all projects from the database.
     *
     * @return A list of ProjectDTOs.
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        log.info("Fetching all projects");
        return projectRepository.findAllByOrderByCreatedDesc().stream()
                .map(projectMapper::toDTO)
                .map(this::calculateProgressForProject)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the 5 most recently created projects.
     *
     * @return A list of up to 5 recent ProjectDTOs.
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getRecentProjects() {
        log.info("Fetching the 5 most recent projects");
        return projectRepository.findTop5ByOrderByCreatedDesc().stream()
                .map(projectMapper::toDTO)
                .map(this::calculateProgressForProject)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all projects where the given user is either the creator or an assigned member.
     *
     * @param userEmail The email of the user.
     * @return A list of ProjectDTOs.
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsForUser(String userEmail) {
        log.info("Fetching projects for user email: {}", userEmail);
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        return projectRepository.findProjectsByUserInvolvement(user.getId()).stream()
                .map(projectMapper::toDTO)
                .map(this::calculateProgressForProject)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific project by its ID.
     *
     * @param id The ID of the project.
     * @return The ProjectDTO.
     * @throws RuntimeException if the project does not exist.
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        log.info("Fetching project with id: {}", id);
        return projectRepository.findById(id)
                .map(projectMapper::toDTO)
                .map(this::calculateProgressForProject)
                .orElseThrow(() -> new ResourceNotFoundException("Project with id " + id + " not found"));
    }

    /**
     * Updates an existing project's core details and its assigned members.
     * Members missing from the incoming list are removed, new ones are added, and existing ones are updated.
     *
     * @param id The ID of the project to update.
     * @param projectDTO The new project data.
     * @return The updated ProjectDTO.
     * @throws RuntimeException if the project does not exist.
     */
    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        log.info("Updating project with id: {}", id);
        
        ProjectEntity existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project with id " + id + " not found"));

        // Update basic fields
        existingProject.setName(projectDTO.getName());
        existingProject.setDescription(projectDTO.getDescription());
        existingProject.setColor(projectDTO.getColor());
        existingProject.setDueDate(projectDTO.getDueDate());
        existingProject.setCompleted(projectDTO.getCompleted());
        existingProject.setModified(LocalDate.now());

        // Clear existing members (orphanRemoval=true in entity will handle database deletion)
        existingProject.getMembers().clear();
        
        // Assign new members
        assignMembersToProject(existingProject, projectDTO.getMembers());

        ProjectEntity updatedProject = projectRepository.save(existingProject);
        return calculateProgressForProject(projectMapper.toDTO(updatedProject));
    }

    /**
     * Deletes a project from the database.
     * Also deletes all associated tasks and memberships.
     *
     * @param id The ID of the project to delete.
     * @throws RuntimeException if the project does not exist.
     */
    @Transactional
    public void deleteProject(Long id) {
        log.info("Deleting project with id: {}", id);
        
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project with id " + id + " not found");
        }

        try {
            // First, delete all tasks associated with the project to avoid constraint violations
            taskRepository.deleteByProjectId(id);
            // Now, delete the project
            projectRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceInUseException("Cannot delete project. It is still being referenced by other records.");
        }
    }
    
    /**
     * Helper method to map a list of ProjectUserDTOs to ProjectUserEntities and add them to a project.
     * 
     * @param projectEntity The target project entity.
     * @param memberDTOs The list of member DTOs containing user IDs and roles.
     */
    private void assignMembersToProject(ProjectEntity projectEntity, List<ProjectUserDTO> memberDTOs) {
        if (memberDTOs != null && !memberDTOs.isEmpty()) {
            if (projectEntity.getMembers() == null) {
                projectEntity.setMembers(new ArrayList<>());
            }
            
            for (ProjectUserDTO memberDTO : memberDTOs) {
                UserEntity user = userRepository.findById(memberDTO.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User with id " + memberDTO.getUserId() + " not found"));

                String role = memberDTO.getRole() != null ? memberDTO.getRole() : "MEMBER";
                
                ProjectUserEntity projectUser = ProjectUserEntity.builder()
                        .project(projectEntity)
                        .user(user)
                        .role(role)
                        .build();
                projectEntity.getMembers().add(projectUser);
            }
        }
    }

    /**
     * Helper method to calculate total tasks, completed tasks, and overall progress percentage.
     */
    private ProjectDTO calculateProgressForProject(ProjectDTO dto) {
        int total = taskRepository.countByProjectId(dto.getId());
        int completed = taskRepository.countByProjectIdAndIsCompleted(dto.getId(), true);
        
        dto.setTotalTasks(total);
        dto.setCompletedTasks(completed);
        dto.setTasksDue(total - completed);

        if (total == 0) {
            dto.setProgress(0); // Prevent divide-by-zero error
        } else {
            // Calculate percentage: (completed / total) * 100
            int progress = (int) Math.round(((double) completed / total) * 100);
            dto.setProgress(progress);
        }

        return dto;
    }
}
