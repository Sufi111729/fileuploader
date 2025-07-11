
package com.Fileupload.sufi.service;

import com.Fileupload.sufi.config.FileStorageProperties;
import com.Fileupload.sufi.repository.UploadedFileRepository;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

@Service
public class FileStorageService {
    private final UploadedFileRepository fileRepository;
    private final Path fileStorageLocation;

    public FileStorageService(FileStorageProperties fileStorageProperties, UploadedFileRepository fileRepository) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        this.fileRepository = fileRepository;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot upload empty file");
            }
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid file path: " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName, ex);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Runs every hour
    public void deleteOldFiles() {
        try {
            Files.list(fileStorageLocation).forEach(path -> {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                    Instant fileTime = attrs.creationTime().toInstant();
                    if (Instant.now().minusSeconds(86400).isAfter(fileTime)) { // 86400 seconds = 1 day
                        Files.delete(path);
                    }
                } catch (Exception e) {
                    // Handle exception (log or ignore)
                }
            });
        } catch (IOException e) {
            // Handle exception (log or ignore)
        }
    }

    public Path getFileStorageLocation() {
        return this.fileStorageLocation;
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }
}
