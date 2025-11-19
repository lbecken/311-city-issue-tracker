package gov.lby.cityissuetracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local filesystem implementation of FileStorageService.
 * Stores files in a configurable directory on the local machine.
 */
@Service
@Profile("!production")
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:/uploads}")
    private String baseUrl;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("Upload directory initialized at: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IOException("Cannot store empty file");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // Create subdirectory if specified
        Path targetDir = uploadPath;
        if (subDirectory != null && !subDirectory.isEmpty()) {
            targetDir = uploadPath.resolve(subDirectory);
            Files.createDirectories(targetDir);
        }

        // Store file
        Path targetPath = targetDir.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path
        String relativePath = subDirectory != null && !subDirectory.isEmpty()
                ? subDirectory + "/" + filename
                : filename;

        log.info("Stored file: {} at {}", originalFilename, relativePath);
        return relativePath;
    }

    @Override
    public Resource getFile(String filePath) throws IOException {
        try {
            Path path = uploadPath.resolve(filePath).normalize();

            // Security check - ensure the path is within upload directory
            if (!path.startsWith(uploadPath)) {
                throw new IOException("Access denied: Invalid file path");
            }

            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("File not found: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new IOException("Invalid file path: " + filePath, e);
        }
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        Path path = uploadPath.resolve(filePath).normalize();

        // Security check
        if (!path.startsWith(uploadPath)) {
            throw new IOException("Access denied: Invalid file path");
        }

        if (Files.exists(path)) {
            Files.delete(path);
            log.info("Deleted file: {}", filePath);
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        Path path = uploadPath.resolve(filePath).normalize();
        return path.startsWith(uploadPath) && Files.exists(path);
    }

    @Override
    public String getFileUrl(String filePath) {
        return baseUrl + "/" + filePath;
    }
}
