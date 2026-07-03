package com.taskqueue.service;

import com.taskqueue.entity.TaskLog;
import com.taskqueue.repository.TaskLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskLogService {

    private final TaskLogRepository taskLogRepository;

    @Transactional
    public void addLog(Long taskId, String message, String level) {
        try {
            TaskLog log = TaskLog.builder()
                .taskId(taskId)
                .message(message)
                .level(TaskLog.LogLevel.valueOf(level))
                .build();
            taskLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to add task log for task {}", taskId, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<TaskLog> getTaskLogs(Long taskId, Pageable pageable) {
        return taskLogRepository.findByTaskId(taskId, pageable);
    }

    @Transactional(readOnly = true)
    public List<TaskLog> getTaskLogsList(Long taskId) {
        return taskLogRepository.findByTaskId(taskId);
    }

}