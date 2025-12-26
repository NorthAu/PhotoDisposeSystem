package com.photodisposesystem.controller;

import com.photodisposesystem.dto.TaskRequest;
import com.photodisposesystem.dto.TaskResponse;
import com.photodisposesystem.model.TaskRecord;
import com.photodisposesystem.model.TaskType;
import com.photodisposesystem.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final TaskService taskService;

    public AiController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/enhance")
    public ResponseEntity<TaskResponse> enhance(@RequestParam Long userId, @RequestParam Long imageId) {
        TaskRequest request = new TaskRequest();
        request.setImageId(imageId);
        request.setTaskType(TaskType.ENHANCE);
        TaskRecord record = taskService.createTask(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(record));
    }

    @PostMapping("/style-transfer")
    public ResponseEntity<TaskResponse> styleTransfer(@RequestParam Long userId,
                                                      @RequestParam Long imageId,
                                                      @RequestParam(defaultValue = "van-gogh") String style) {
        TaskRequest request = new TaskRequest();
        request.setImageId(imageId);
        request.setTaskType(TaskType.STYLE_TRANSFER);
        request.setOption(style);
        TaskRecord record = taskService.createTask(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(record));
    }

    @PostMapping("/background-replace")
    public ResponseEntity<TaskResponse> backgroundReplace(@RequestParam Long userId,
                                                          @RequestParam Long imageId,
                                                          @RequestParam(defaultValue = "studio") String template) {
        TaskRequest request = new TaskRequest();
        request.setImageId(imageId);
        request.setTaskType(TaskType.BACKGROUND_REPLACE);
        request.setOption(template);
        TaskRecord record = taskService.createTask(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(record));
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
    public ResponseEntity<String> handleAiError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
