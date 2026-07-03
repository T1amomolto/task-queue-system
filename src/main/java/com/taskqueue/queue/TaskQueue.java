package com.taskqueue.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskqueue.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskQueue {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String QUEUE_PREFIX = "task:queue:";
    private static final String PROCESSING_PREFIX = "task:processing:";
    private static final long QUEUE_TIMEOUT = 86400; // 24小时

    public void enqueue(Task task) {
        try {
            String key = QUEUE_PREFIX + task.getPriority().ordinal();
            String value = objectMapper.writeValueAsString(task);
            redisTemplate.opsForList().rightPush(key, value);
            log.info("Task {} enqueued with priority {}", task.getId(), task.getPriority());
        } catch (Exception e) {
            log.error("Failed to enqueue task {}", task.getId(), e);
            throw new RuntimeException("Enqueue failed", e);
        }
    }

    public String dequeue(Task.TaskPriority priority) {
        String key = QUEUE_PREFIX + priority.ordinal();
        return redisTemplate.opsForList().leftPop(key, 1, TimeUnit.SECONDS);
    }

    public void markProcessing(Long taskId, String taskData) {
        String key = PROCESSING_PREFIX + taskId;
        redisTemplate.opsForValue().set(key, taskData, QUEUE_TIMEOUT, TimeUnit.SECONDS);
        log.debug("Task {} marked as processing", taskId);
    }

    public void removeProcessing(Long taskId) {
        String key = PROCESSING_PREFIX + taskId;
        redisTemplate.delete(key);
        log.debug("Task {} removed from processing", taskId);
    }

    public String getProcessingTask(Long taskId) {
        String key = PROCESSING_PREFIX + taskId;
        return redisTemplate.opsForValue().get(key);
    }

    public void moveToRetry(Task task) {
        try {
            String retryKey = QUEUE_PREFIX + Task.TaskPriority.HIGH.ordinal();
            String value = objectMapper.writeValueAsString(task);
            redisTemplate.opsForList().rightPush(retryKey, value);
            log.info("Task {} moved to retry queue", task.getId());
        } catch (Exception e) {
            log.error("Failed to move task {} to retry", task.getId(), e);
        }
    }

    public long getQueueSize(Task.TaskPriority priority) {
        String key = QUEUE_PREFIX + priority.ordinal();
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }

}