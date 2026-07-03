package com.taskqueue.service;

import com.taskqueue.dto.TaskCreateRequest;
import com.taskqueue.dto.TaskResponse;
import com.taskqueue.entity.Task;
import com.taskqueue.entity.Task.TaskStatus;
import com.taskqueue.repository.TaskRepository;
import com.taskqueue.queue.TaskQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskQueue taskQueue;
    private final TaskLogService taskLogService;

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        Task task = Task.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .payload(request.getPayload())
            .priority(request.getPriority() != null ? request.getPriority() : Task.TaskPriority.MEDIUM)
            .status(TaskStatus.PENDING)
            .maxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3)
            .build();

        Task savedTask = taskRepository.save(task);
        taskQueue.enqueue(savedTask);
        taskLogService.addLog(savedTask.getId(), "Task created successfully", "INFO");

        log.info("Created task: {}", savedTask.getId());
        return TaskResponse.fromEntity(savedTask);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        return TaskResponse.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
            .map(TaskResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listTasksByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable)
            .map(TaskResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listTasksByPriority(Task.TaskPriority priority, Pageable pageable) {
        return taskRepository.findByPriority(priority, pageable)
            .map(TaskResponse::fromEntity);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        task.setStatus(status);
        if (status == TaskStatus.PROCESSING) {
            task.setStartedAt(LocalDateTime.now());
        } else if (status == TaskStatus.SUCCESS || status == TaskStatus.FAILED) {
            task.setCompletedAt(LocalDateTime.now());
            if (task.getStartedAt() != null) {
                task.setExecutionTime(
                    java.time.temporal.ChronoUnit.MILLIS.between(task.getStartedAt(), task.getCompletedAt())
                );
            }
        }

        Task updated = taskRepository.save(task);
        taskLogService.addLog(taskId, "Task status updated to " + status, "INFO");

        log.info("Updated task {} status to {}", taskId, status);
        return TaskResponse.fromEntity(updated);
    }

    @Transactional
    public TaskResponse cancelTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.RETRYING) {
            throw new RuntimeException("Cannot cancel task in status: " + task.getStatus());
        }

        task.setStatus(TaskStatus.CANCELLED);
        Task updated = taskRepository.save(task);
        taskLogService.addLog(taskId, "Task cancelled", "INFO");

        log.info("Cancelled task {}", taskId);
        return TaskResponse.fromEntity(updated);
    }

    @Transactional
    public void handleTaskFailure(Long taskId, String errorMessage) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        task.setErrorMessage(errorMessage);
        task.setRetryCount(task.getRetryCount() + 1);

        if (task.getRetryCount() < task.getMaxRetries()) {
            task.setStatus(TaskStatus.RETRYING);
            taskQueue.moveToRetry(task);
            taskLogService.addLog(taskId, "Task retry attempt " + task.getRetryCount(), "WARN");
        } else {
            task.setStatus(TaskStatus.FAILED);
            task.setCompletedAt(LocalDateTime.now());
            taskLogService.addLog(taskId, "Task failed after " + task.getMaxRetries() + " retries", "ERROR");
        }

        taskRepository.save(task);
    }

    @Transactional
    public void handleTaskSuccess(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        task.setStatus(TaskStatus.SUCCESS);
        task.setCompletedAt(LocalDateTime.now());
        if (task.getStartedAt() != null) {
            task.setExecutionTime(
                java.time.temporal.ChronoUnit.MILLIS.between(task.getStartedAt(), task.getCompletedAt())
            );
        }
        taskRepository.save(task);
        taskLogService.addLog(taskId, "Task completed successfully", "INFO");

        log.info("Task {} completed successfully", taskId);
    }

    @Transactional(readOnly = true)
    public List<Task> getPendingTasks(int limit) {
        return taskRepository.findPendingTasks(limit);
    }

    @Transactional(readOnly = true)
    public long getTaskCount(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByDateRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return taskRepository.findByCreatedAtBetween(startTime, endTime, pageable)
            .map(TaskResponse::fromEntity);
    }

}