package com.taskqueue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskqueue.entity.Task;
import com.taskqueue.queue.TaskQueue;
import com.taskqueue.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "task-queue.worker.enabled", havingValue = "true", matchIfMissing = true)
public class TaskWorker {

    private final TaskQueue taskQueue;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 10;

    @Scheduled(fixedDelayString = "${task-queue.worker.process-interval:1000}")
    public void processTaskQueue() {
        try {
            processTasks(Task.TaskPriority.URGENT);
            processTasks(Task.TaskPriority.HIGH);
            processTasks(Task.TaskPriority.MEDIUM);
            processTasks(Task.TaskPriority.LOW);
        } catch (Exception e) {
            log.error("Error processing task queue", e);
        }
    }

    private void processTasks(Task.TaskPriority priority) {
        for (int i = 0; i < BATCH_SIZE; i++) {
            String taskData = taskQueue.dequeue(priority);
            if (taskData == null) {
                break;
            }

            try {
                Task task = objectMapper.readValue(taskData, Task.class);
                executeTask(task);
            } catch (Exception e) {
                log.error("Failed to process task", e);
            }
        }
    }

    private void executeTask(Task task) {
        Long taskId = task.getId();
        log.info("Executing task: {}", taskId);

        try {
            taskService.updateTaskStatus(taskId, Task.TaskStatus.PROCESSING);
            taskQueue.markProcessing(taskId, "");

            simulateTaskExecution(task);

            taskService.handleTaskSuccess(taskId);
            log.info("Task {} completed successfully", taskId);

        } catch (Exception e) {
            log.error("Task {} execution failed", taskId, e);
            taskService.handleTaskFailure(taskId, e.getMessage());
        } finally {
            taskQueue.removeProcessing(taskId);
        }
    }

    private void simulateTaskExecution(Task task) throws InterruptedException {
        int processingTime = (int) (Math.random() * 5000) + 1000;
        Thread.sleep(processingTime);

        if (Math.random() < 0.05) {
            throw new RuntimeException("Simulated task execution failure");
        }

        log.debug("Task payload: {}", task.getPayload());
    }

}