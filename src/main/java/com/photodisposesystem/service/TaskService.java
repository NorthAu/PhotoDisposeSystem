package com.photodisposesystem.service;

import com.photodisposesystem.dto.TaskRequest;
import com.photodisposesystem.model.ImageAsset;
import com.photodisposesystem.model.TaskRecord;
import com.photodisposesystem.model.TaskStatus;
import com.photodisposesystem.model.TaskType;
import com.photodisposesystem.model.User;
import com.photodisposesystem.repository.ImageAssetRepository;
import com.photodisposesystem.repository.TaskRecordRepository;
import com.photodisposesystem.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRecordRepository taskRecordRepository;
    private final UserRepository userRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final StorageService storageService;
    private final ImageProcessingService imageProcessingService;
    private final AiInferenceService aiInferenceService;

    public TaskService(TaskRecordRepository taskRecordRepository,
                       UserRepository userRepository,
                       ImageAssetRepository imageAssetRepository,
                       StorageService storageService,
                       ImageProcessingService imageProcessingService,
                       AiInferenceService aiInferenceService) {
        this.taskRecordRepository = taskRecordRepository;
        this.userRepository = userRepository;
        this.imageAssetRepository = imageAssetRepository;
        this.storageService = storageService;
        this.imageProcessingService = imageProcessingService;
        this.aiInferenceService = aiInferenceService;
    }

    public TaskRecord createTask(Long userId, TaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ImageAsset image = imageAssetRepository.findById(request.getImageId())
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        TaskRecord record = new TaskRecord();
        record.setUser(user);
        record.setImageAsset(image);
        record.setTaskType(request.getTaskType());
        record.setStatus(TaskStatus.PROCESSING);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        taskRecordRepository.save(record);

        processTask(record, request.getOption());
        return record;
    }

    public List<TaskRecord> getHistory(Long userId) {
        return taskRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private void processTask(TaskRecord record, String option) {
        try {
            TaskType type = record.getTaskType();
            if (type == TaskType.ENHANCE) {
                record.setMessage(aiInferenceService.requestEnhancement(record.getImageAsset().getId()));
            } else if (type == TaskType.STYLE_TRANSFER) {
                record.setMessage(aiInferenceService.requestStyleTransfer(record.getImageAsset().getId(), option));
            } else if (type == TaskType.BACKGROUND_REPLACE) {
                record.setMessage(aiInferenceService.requestBackgroundReplace(record.getImageAsset().getId(), option));
            } else if (type == TaskType.COMPRESS) {
                Path inputPath = storageService.load(record.getImageAsset().getStoragePath());
                imageProcessingService.compressImage(inputPath, 0.7f);
                record.setMessage("Compression completed");
            } else if (type == TaskType.CONVERT) {
                Path inputPath = storageService.load(record.getImageAsset().getStoragePath());
                imageProcessingService.convertImage(inputPath, option == null ? "png" : option);
                record.setMessage("Conversion completed");
            }
            record.setStatus(TaskStatus.FINISHED);
        } catch (IOException | IllegalArgumentException ex) {
            record.setStatus(TaskStatus.FAILED);
            record.setMessage(ex.getMessage());
        }
        record.setUpdatedAt(LocalDateTime.now());
        taskRecordRepository.save(record);
    }
}
