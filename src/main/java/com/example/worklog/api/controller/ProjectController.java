package com.example.worklog.api.controller;

import com.example.worklog.api.dto.ProjectDTO;
import com.example.worklog.application.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Project entities.
 * Exposes endpoints for Create, Read, Update, and Delete (CRUD) operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * POST /api/projects : Creates a new project.
     * The creator's email is securely extracted from the JWT token.
     */
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO, Authentication authentication) {
        // Inject the email from the secure token into the DTO before passing to the service
        projectDTO.setCreatedByUserEmail(authentication.getName());
        ProjectDTO createdProject = projectService.createProject(projectDTO);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    /**
     * GET /api/projects : Retrieves a list of all projects.
     */
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<ProjectDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * GET /api/projects/recent : Retrieves the 5 most recently created projects.
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ProjectDTO>> getRecentProjects() {
        List<ProjectDTO> projects = projectService.getRecentProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * GET /api/projects/my-projects : Retrieves all projects where the authenticated user is the creator or a member.
     */
    @GetMapping("/my-projects")
    public ResponseEntity<List<ProjectDTO>> getMyProjects(Authentication authentication) {
        List<ProjectDTO> projects = projectService.getProjectsForUser(authentication.getName());
        return ResponseEntity.ok(projects);
    }

    /**
     * GET /api/projects/{id} : Retrieves a specific project by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        ProjectDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    /**
     * PUT /api/projects/{id} : Updates an existing project.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        ProjectDTO updatedProject = projectService.updateProject(id, projectDTO);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * DELETE /api/projects/{id} : Deletes a project.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
