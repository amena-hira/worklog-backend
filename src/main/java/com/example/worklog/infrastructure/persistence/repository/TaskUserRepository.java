package com.example.worklog.infrastructure.persistence.repository;

import com.example.worklog.infrastructure.persistence.entity.TaskUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing task assignments to users.
 */
@Repository
public interface TaskUserRepository extends JpaRepository<TaskUserEntity, Long> {
    
    /** Finds all user assignments for a specific task. */
    List<TaskUserEntity> findByTaskId(Long taskId);
    
    /** Deletes all user assignments for a specific task. */
    void deleteByTaskId(Long taskId);
    
    /** Deletes all task assignments for a specific project. */
    void deleteByTaskProjectId(Long projectId);
}
