package com.taskqueue.dto;

import com.taskqueue.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private Task.TaskStatus status;

    private Task.TaskPriority priority;

    private String payload;

    private Integer retryCount;

    private Integer maxRetries;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String errorMessage;

    private Long executionTime;

    public static TaskResponse fromEntity(Task task) {
        return TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .priority(task.getPriority())
            .payload(task.getPayload())
            .retryCount(task.getRetryCount())
            .maxRetries(task.getMaxRetries())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .startedAt(task.getStartedAt())
            .completedAt(task.getCompletedAt())
            .errorMessage(task.getErrorMessage())
            .executionTime(task.getExecutionTime())
            .build();
    }

}