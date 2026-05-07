package com.example.worklog.application.service;

import com.example.worklog.api.dto.ProjectDTO;
import com.example.worklog.api.dto.ProjectUserDTO;
import com.example.worklog.api.mapper.ProjectMapper;
import com.example.worklog.infrastructure.persistence.entity.ProjectEntity;
import com.example.worklog.infrastructure.persistence.entity.ProjectUserEntity;
import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import com.example.worklog.infrastructure.persistence.repository.ProjectRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;

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
                    .orElseThrow(() -> new RuntimeException("Creator user not found with email: " + projectDTO.getCreatedByUserEmail()));
            projectEntity.setCreatedBy(creator);
        }
        
        // Handle assigning members to the project
        assignMembersToProject(projectEntity, projectDTO.getMembers());

        ProjectEntity savedProject = projectRepository.save(projectEntity);
        return projectMapper.toDTO(savedProject);
    }

    /**
     * Retrieves all projects from the database.
     *
     * @return A list of ProjectDTOs.
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        log.info("Fetching all projects");
        return projectRepository.findAll().stream()
                .map(projectMapper::toDTO)
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
                .orElseThrow(() -> new RuntimeException("Project with id " + id + " not found"));
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
                .orElseThrow(() -> new RuntimeException("Project with id " + id + " not found"));

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
        return projectMapper.toDTO(updatedProject);
    }

    /**
     * Deletes a project from the database.
     * Note: Due to CascadeType.ALL, this will also delete all associated tasks and memberships.
     *
     * @param id The ID of the project to delete.
     * @throws RuntimeException if the project does not exist.
     */
    @Transactional
    public void deleteProject(Long id) {
        log.info("Deleting project with id: {}", id);
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project with id " + id + " not found");
        }
        projectRepository.deleteById(id);
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
                        .orElseThrow(() -> new RuntimeException("User with id " + memberDTO.getUserId() + " not found"));

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
}
