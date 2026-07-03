package com.taskqueue.service;

import com.taskqueue.entity.Task;
import com.taskqueue.queue.TaskQueue;
import com.taskqueue.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskMonitor {

    private final TaskRepository taskRepository;
    private final TaskQueue taskQueue;

    @Scheduled(fixedDelayString = "${task-queue.monitor.check-interval:60000}")
    public void monitorQueueStatus() {
        try {
            long pendingCount = taskRepository.countByStatus(Task.TaskStatus.PENDING);
            long processingCount = taskRepository.countByStatus(Task.TaskStatus.PROCESSING);
            long successCount = taskRepository.countByStatus(Task.TaskStatus.SUCCESS);
            long failedCount = taskRepository.countByStatus(Task.TaskStatus.FAILED);

            long urgentQueueSize = taskQueue.getQueueSize(Task.TaskPriority.URGENT);
            long highQueueSize = taskQueue.getQueueSize(Task.TaskPriority.HIGH);
            long mediumQueueSize = taskQueue.getQueueSize(Task.TaskPriority.MEDIUM);
            long lowQueueSize = taskQueue.getQueueSize(Task.TaskPriority.LOW);

            log.info("=== Task Queue Status ===");
            log.info("Task Status - PENDING: {}, PROCESSING: {}, SUCCESS: {}, FAILED: {}",
                pendingCount, processingCount, successCount, failedCount);
            log.info("Queue Size - URGENT: {}, HIGH: {}, MEDIUM: {}, LOW: {}",
                urgentQueueSize, highQueueSize, mediumQueueSize, lowQueueSize);

            checkAlerts(pendingCount, processingCount);
        } catch (Exception e) {
            log.error("Error monitoring queue status", e);
        }
    }

    private void checkAlerts(long pendingCount, long processingCount) {
        if (pendingCount > 10000) {
            log.warn("ALERT: Too many pending tasks: {}", pendingCount);
        }

        if (processingCount == 0 && pendingCount > 0) {
            log.warn("ALERT: No tasks are being processed but {} pending tasks exist", pendingCount);
        }
    }

}