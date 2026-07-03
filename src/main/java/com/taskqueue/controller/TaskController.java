package com.taskqueue.controller;

import com.taskqueue.dto.ApiResponse;
import com.taskqueue.dto.TaskCreateRequest;
import com.taskqueue.dto.TaskResponse;
import com.taskqueue.entity.Task;
import com.taskqueue.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@RequestBody TaskCreateRequest request) {
        log.info("Creating task: {}", request.getTitle());
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Task created successfully", response));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable Long taskId) {
        log.info("Fetching task: {}", taskId);
        TaskResponse response = taskService.getTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> listTasks(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        log.info("Listing tasks with page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponse> response = taskService.listTasks(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> listTasksByStatus(
        @PathVariable Task.TaskStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        log.info("Listing tasks by status: {}", status);
        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponse> response = taskService.listTasksByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> listTasksByPriority(
        @PathVariable Task.TaskPriority priority,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        log.info("Listing tasks by priority: {}", priority);
        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponse> response = taskService.listTasksByPriority(priority, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> listTasksByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        log.info("Listing tasks by date range: {} to {}", startTime, endTime);
        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponse> response = taskService.getTasksByDateRange(startTime, endTime, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
        @PathVariable Long taskId,
        @RequestParam Task.TaskStatus status) {
        log.info("Updating task {} status to: {}", taskId, status);
        TaskResponse response = taskService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(ApiResponse.success("Task status updated", response));
    }

    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<ApiResponse<TaskResponse>> cancelTask(@PathVariable Long taskId) {
        log.info("Cancelling task: {}", taskId);
        TaskResponse response = taskService.cancelTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task cancelled", response));
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<ApiResponse<?>> getTaskStats() {
        log.info("Fetching task statistics");
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "pending", taskService.getTaskCount(Task.TaskStatus.PENDING),
            "processing", taskService.getTaskCount(Task.TaskStatus.PROCESSING),
            "success", taskService.getTaskCount(Task.TaskStatus.SUCCESS),
            "failed", taskService.getTaskCount(Task.TaskStatus.FAILED),
            "retrying", taskService.getTaskCount(Task.TaskStatus.RETRYING)
        )));
    }

}