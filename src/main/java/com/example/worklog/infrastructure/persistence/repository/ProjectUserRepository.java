package com.example.worklog.infrastructure.persistence.repository;

import com.example.worklog.infrastructure.persistence.entity.ProjectUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing project memberships and roles.
 */
@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectUserEntity, Long> {
    
    /** Finds all projects a user is assigned to. */
    List<ProjectUserEntity> findByUserId(Long userId);
    
    /** Finds all members of a specific project. */
    List<ProjectUserEntity> findByProjectId(Long projectId);
    
    /** Deletes all memberships for a specific project. */
    void deleteByProjectId(Long projectId);
}
