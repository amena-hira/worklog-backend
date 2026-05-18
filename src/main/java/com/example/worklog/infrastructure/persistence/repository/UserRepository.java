package com.example.worklog.infrastructure.persistence.repository;

import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserEntity database operations.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    /** Finds all users ordered by ID descending (newest first). */
    List<UserEntity> findAllByOrderByIdDesc();

    /** Finds a user by their exact email address. */
    Optional<UserEntity> findByEmail(String email);
}
