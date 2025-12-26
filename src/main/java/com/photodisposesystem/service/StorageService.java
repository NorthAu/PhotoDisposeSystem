package com.photodisposesystem.service;

import com.photodisposesystem.config.AppProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private final Path rootLocation;

    public StorageService(AppProperties appProperties) {
        this.rootLocation = Paths.get(appProperties.getStoragePath()).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        Files.createDirectories(rootLocation);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path destinationFile = rootLocation.resolve(filename).normalize();
        Files.copy(file.getInputStream(), destinationFile);
        return destinationFile.toString();
    }

    public Path load(String storagePath) {
        return Paths.get(storagePath).toAbsolutePath().normalize();
    }
}
