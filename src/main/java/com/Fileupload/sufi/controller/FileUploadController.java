package com.Fileupload.sufi.controller;

import com.Fileupload.sufi.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
public class FileUploadController {
    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/")
    public String showUploadForm(Model model) {
        List<String> fileNames = new ArrayList<>();
        Path uploadDir = fileStorageService.getFileStorageLocation();
        File folder = uploadDir.toFile();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                fileNames.add(file.getName());
            }
        }
        model.addAttribute("files", fileNames);
        return "index";
    }
    
    @GetMapping("/upload")
    public String showUploadPage() {
        return "redirect:/upload.html";
    }
	/**
	 * Handles file upload requests.
	 *
	 * @param file the uploaded file
	 * @param model the model to add attributes for the view
	 * @return redirect to the home page with a success or error message
	 */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String fileName = fileStorageService.storeFile(file);
            model.addAttribute("message", "File uploaded successfully: " + fileName);
        } catch (Exception e) {
            model.addAttribute("message", "Failed to upload file: " + e.getMessage());
        }
        return "redirect:/";
    }
   



    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodeFilename(resource.getFilename()) + "\"")
                .body(resource);
    }

    @PostMapping("/delete/{fileName:.+}")
    public String deleteFile(@PathVariable String fileName, Model model) {
        try {
            Path filePath = fileStorageService.getFileStorageLocation().resolve(fileName);
            Files.deleteIfExists(filePath);
            model.addAttribute("message", "File deleted: " + fileName);
        } catch (Exception e) {
            model.addAttribute("message", "Failed to delete file: " + e.getMessage());
        }
        return showUploadForm(model);
    }

    @PostMapping("/update/{fileName:.+}")
    public String updateFile(@PathVariable String fileName, @RequestParam("file") MultipartFile file, Model model) {
        try {
            Path targetLocation = fileStorageService.getFileStorageLocation().resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            model.addAttribute("message", "File updated: " + fileName);
        } catch (Exception e) {
            model.addAttribute("message", "Failed to update file: " + e.getMessage());
        }
        return showUploadForm(model);
    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<Resource> viewFile(@PathVariable String fileName) {
        Resource file = fileStorageService.loadFileAsResource(fileName);
        String contentType = "application/octet-stream";
        if (fileName.endsWith(".pdf")) contentType = "application/pdf";
        else if (fileName.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$")) contentType = "image/" + fileName.substring(fileName.lastIndexOf('.') + 1);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(file);
    }


    @GetMapping("/print/{fileName:.+}")
    public ResponseEntity<Resource> printFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = determineContentType(resource);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + encodeFilename(resource.getFilename()) + "\"")
                .header("X-Print-Optimized", "true")
                .body(resource);
    }

    private String determineContentType(Resource resource) {
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        try {
            Path filePath = Paths.get(resource.getFile().getAbsolutePath());
            contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = URLConnection.guessContentTypeFromName(resource.getFilename());
            }
        } catch (IOException e) {
            log.warn("Could not determine content type for file: {}", resource.getFilename(), e);
        }
        return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    
    }private String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        } catch (Exception e) {
            log.warn("Failed to encode filename: {}", filename, e);
            return filename;
        }
    }

}
