package com.example.worklog.infrastructure.persistence.repository;

import com.example.worklog.infrastructure.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ProjectEntity database operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    
    /** Finds the 5 most recently created projects. */
    List<ProjectEntity> findTop5ByOrderByCreatedDesc();

    /** 
     * Finds all projects where a specific user is involved:
     * 1. As the creator of the project
     * 2. As an assigned member of the project
     * 3. As an assignee on ANY task within the project
     */
    @Query("SELECT DISTINCT p FROM ProjectEntity p " +
           "LEFT JOIN p.members m " +
           "LEFT JOIN p.tasks t " +
           "LEFT JOIN t.assignees ta " +
           "WHERE p.createdBy.id = :userId " +
           "OR m.user.id = :userId " +
           "OR ta.user.id = :userId")
    List<ProjectEntity> findProjectsByUserInvolvement(@Param("userId") Long userId);
}
