package com.photodisposesystem.controller;

import com.photodisposesystem.dto.ImageUploadResponse;
import com.photodisposesystem.model.ImageAsset;
import com.photodisposesystem.model.User;
import com.photodisposesystem.repository.ImageAssetRepository;
import com.photodisposesystem.repository.UserRepository;
import com.photodisposesystem.service.StorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final StorageService storageService;
    private final ImageAssetRepository imageAssetRepository;
    private final UserRepository userRepository;

    public ImageController(StorageService storageService,
                           ImageAssetRepository imageAssetRepository,
                           UserRepository userRepository) {
        this.storageService = storageService;
        this.imageAssetRepository = imageAssetRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponse> upload(@RequestParam("userId") Long userId,
                                                      @RequestParam("file") MultipartFile file) throws IOException {
        validateFile(file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String storagePath = storageService.store(file);
        ImageAsset asset = new ImageAsset();
        asset.setUser(user);
        asset.setFilename(file.getOriginalFilename());
        asset.setContentType(file.getContentType());
        asset.setSize(file.getSize());
        asset.setStoragePath(storagePath);
        asset.setUploadedAt(LocalDateTime.now());
        ImageAsset saved = imageAssetRepository.save(asset);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ImageUploadResponse(saved.getId(), saved.getFilename(), saved.getContentType(),
                        saved.getSize(), saved.getUploadedAt()));
    }

    @GetMapping
    public List<ImageAsset> list(@RequestParam("userId") Long userId) {
        return imageAssetRepository.findByUserId(userId);
    }

    @GetMapping("/{imageId}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long imageId) throws IOException {
        ImageAsset asset = imageAssetRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
        Path path = storageService.load(asset.getStoragePath());
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Stored file not found");
        }
        FileSystemResource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + asset.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(asset.getContentType()))
                .body(resource);
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equalsIgnoreCase(MediaType.IMAGE_JPEG_VALUE)
                        && !contentType.equalsIgnoreCase(MediaType.IMAGE_PNG_VALUE))) {
            throw new IllegalArgumentException("Only JPG and PNG files are supported");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleImageError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
