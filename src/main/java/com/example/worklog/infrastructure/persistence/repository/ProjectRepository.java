package com.example.worklog.infrastructure.persistence.repository;

import com.example.worklog.infrastructure.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ProjectEntity database operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
}
