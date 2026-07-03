package com.taskqueue.service;

import com.taskqueue.dto.TaskCreateRequest;
import com.taskqueue.dto.TaskResponse;
import com.taskqueue.entity.Task;
import com.taskqueue.repository.TaskRepository;
import com.taskqueue.queue.TaskQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskQueue taskQueue;

    @Mock
    private TaskLogService taskLogService;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateTask() {
        TaskCreateRequest request = TaskCreateRequest.builder()
            .title("Test Task")
            .description("Test Description")
            .payload("{\"key\": \"value\"}")
            .priority(Task.TaskPriority.HIGH)
            .maxRetries(3)
            .build();

        Task savedTask = Task.builder()
            .id(1L)
            .title("Test Task")
            .description("Test Description")
            .payload("{\"key\": \"value\"}")
            .priority(Task.TaskPriority.HIGH)
            .status(Task.TaskStatus.PENDING)
            .maxRetries(3)
            .build();

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals(Task.TaskStatus.PENDING, response.getStatus());

        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskQueue, times(1)).enqueue(any(Task.class));
    }

    @Test
    public void testGetTask() {
        Task task = Task.builder()
            .id(1L)
            .title("Test Task")
            .status(Task.TaskStatus.PENDING)
            .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTask(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.getTask(999L));
    }

    @Test
    public void testUpdateTaskStatus() {
        Task task = Task.builder()
            .id(1L)
            .title("Test Task")
            .status(Task.TaskStatus.PENDING)
            .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.updateTaskStatus(1L, Task.TaskStatus.PROCESSING);

        assertNotNull(response);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

}