package com.taskqueue.repository;

import com.taskqueue.entity.TaskLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {

    Page<TaskLog> findByTaskId(Long taskId, Pageable pageable);

    List<TaskLog> findByTaskId(Long taskId);

}