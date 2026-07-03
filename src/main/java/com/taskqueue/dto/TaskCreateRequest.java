package com.taskqueue.dto;

import com.taskqueue.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCreateRequest {

    private String title;

    private String description;

    private String payload;

    private Task.TaskPriority priority;

    private Integer maxRetries;

}