package com.taskqueue.repository;

import com.taskqueue.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(Task.TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(Task.TaskPriority priority, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.status = ?1 ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findByStatusOrderByPriority(Task.TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' OR t.status = 'RETRYING' LIMIT ?1")
    List<Task> findPendingTasks(int limit);

    Page<Task> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    long countByStatus(Task.TaskStatus status);

}