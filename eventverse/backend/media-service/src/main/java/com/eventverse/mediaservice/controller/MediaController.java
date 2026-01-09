package com.eventverse.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    @Value("${media.storage.path}")
    private String storagePath;

    @Value("${server.port:8087}")
    private int serverPort;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
        }

        // Validate file type (images only)
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only image files are allowed"));
        }

        // Generate unique filename
        String fileName = UUID.randomUUID() + ext;

        // Ensure storage directory exists
        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        // Save file
        Path target = Paths.get(storagePath, fileName);
        Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // Return CDN-like URL (via API Gateway or media-service directly)
        // API Gateway routes /media/** to media-service, so we can use gateway URL
        // For static files, use media-service directly or nginx if configured
        String url = "http://localhost:" + serverPort + "/static/" + fileName;

        return ResponseEntity.ok(Map.of("url", url));
    }
}

