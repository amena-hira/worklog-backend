package com.example.worklog.infrastructure.persistence.repository;

import com.example.worklog.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TaskEntity database operations.
 */
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    
    /** Finds tasks belonging to a specific project. */
    List<TaskEntity> findByProjectId(Long projectId);
    
    /** Finds tasks created by a specific user. */
    List<TaskEntity> findByUserId(Long userId);
    
    /** Deletes all tasks associated with a specific project. */
    void deleteByProjectId(Long projectId);
}
