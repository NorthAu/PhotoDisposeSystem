package com.photodisposesystem.controller;

import com.photodisposesystem.dto.BatchTaskRequest;
import com.photodisposesystem.dto.TaskRequest;
import com.photodisposesystem.dto.TaskResponse;
import com.photodisposesystem.model.TaskRecord;
import com.photodisposesystem.model.TaskType;
import com.photodisposesystem.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/compress")
    public ResponseEntity<TaskResponse> compress(@RequestParam Long userId, @RequestParam Long imageId) {
        TaskRequest request = new TaskRequest();
        request.setImageId(imageId);
        request.setTaskType(TaskType.COMPRESS);
        TaskRecord record = taskService.createTask(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(record));
    }

    @PostMapping("/convert")
    public ResponseEntity<TaskResponse> convert(@RequestParam Long userId,
                                                @RequestParam Long imageId,
                                                @RequestParam(defaultValue = "png") String format) {
        TaskRequest request = new TaskRequest();
        request.setImageId(imageId);
        request.setTaskType(TaskType.CONVERT);
        request.setOption(format);
        TaskRecord record = taskService.createTask(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(record));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<TaskResponse>> batch(@Valid @RequestBody BatchTaskRequest request) {
        List<TaskResponse> responses = request.getTasks().stream()
                .map(task -> toResponse(taskService.createTask(request.getUserId(), task)))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/history")
    public List<TaskResponse> history(@RequestParam Long userId) {
        return taskService.getHistory(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TaskResponse toResponse(TaskRecord record) {
        return new TaskResponse(
                record.getId(),
                record.getImageAsset().getId(),
                record.getTaskType(),
                record.getStatus(),
                record.getMessage(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleTaskError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
