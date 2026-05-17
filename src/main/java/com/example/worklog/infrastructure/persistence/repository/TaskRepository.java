package com.example.worklog.infrastructure.persistence.repository;

import com.example.worklog.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for TaskEntity database operations.
 */
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    
    /** Finds tasks belonging to a specific project. */
    List<TaskEntity> findByProjectId(Long projectId);
    
    /** Finds tasks created by a specific user. */
    List<TaskEntity> findByCreatedById(Long userId);
    
    /** Deletes all tasks associated with a specific project. */
    void deleteByProjectId(Long projectId);

    /** Counts all tasks belonging to a specific project. */
    int countByProjectId(Long projectId);

    /** Counts tasks belonging to a specific project filtered by completion status. */
    int countByProjectIdAndIsCompleted(Long projectId, Boolean isCompleted);

    /** 
     * Finds tasks related to a user (either created by them or assigned to them).
     */
    @Query("SELECT DISTINCT t FROM TaskEntity t LEFT JOIN t.assignees a WHERE t.createdBy.id = :userId OR a.user.id = :userId")
    List<TaskEntity> findTasksByUserInvolvement(@Param("userId") Long userId);

    /** 
     * Finds tasks due today that are related to a user (either created by them or assigned to them).
     */
    @Query("SELECT DISTINCT t FROM TaskEntity t LEFT JOIN t.assignees a WHERE (t.createdBy.id = :userId OR a.user.id = :userId) AND t.dueDate = :dueDate")
    List<TaskEntity> findTasksDueTodayByUserInvolvement(@Param("userId") Long userId, @Param("dueDate") LocalDate dueDate);

    // --- Methods for User Task Statistics ---

    /** Counts all tasks a user is involved in (creator or assignee). */
    @Query("SELECT COUNT(DISTINCT t) FROM TaskEntity t LEFT JOIN t.assignees a WHERE t.createdBy.id = :userId OR a.user.id = :userId")
    int countTasksByUserInvolvement(@Param("userId") Long userId);

    /** Counts tasks a user is involved in, filtered by completion status. */
    @Query("SELECT COUNT(DISTINCT t) FROM TaskEntity t LEFT JOIN t.assignees a WHERE (t.createdBy.id = :userId OR a.user.id = :userId) AND t.isCompleted = :isCompleted")
    int countTasksByUserInvolvementAndIsCompleted(@Param("userId") Long userId, @Param("isCompleted") boolean isCompleted);

    /** Counts incomplete tasks a user is involved in that are past their due date. */
    @Query("SELECT COUNT(DISTINCT t) FROM TaskEntity t LEFT JOIN t.assignees a WHERE (t.createdBy.id = :userId OR a.user.id = :userId) AND t.isCompleted = false AND t.dueDate < :currentDate")
    int countOverdueTasksByUserInvolvement(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);
}
