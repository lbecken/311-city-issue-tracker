package gov.lby.cityissuetracker.service;

import gov.lby.cityissuetracker.dto.ImageResponse;
import gov.lby.cityissuetracker.entity.IssueImage;
import gov.lby.cityissuetracker.exception.IssueNotFoundException;
import gov.lby.cityissuetracker.repository.IssueImageRepository;
import gov.lby.cityissuetracker.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final IssueImageRepository imageRepository;
    private final IssueRepository issueRepository;
    private final FileStorageService fileStorageService;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Transactional
    public ImageResponse uploadImage(UUID issueId, MultipartFile file) throws IOException {
        // Verify issue exists
        if (!issueRepository.existsById(issueId)) {
            throw new IssueNotFoundException(issueId);
        }

        // Validate file
        validateFile(file);

        // Store file
        String filePath = fileStorageService.storeFile(file, "issues/" + issueId);

        // Create database record
        IssueImage image = IssueImage.builder()
                .issueId(issueId)
                .filename(filePath.substring(filePath.lastIndexOf("/") + 1))
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(filePath)
                .build();

        image = imageRepository.save(image);
        log.info("Uploaded image {} for issue {}", image.getId(), issueId);

        return toResponse(image);
    }

    public List<ImageResponse> getImagesForIssue(UUID issueId) {
        // Verify issue exists
        if (!issueRepository.existsById(issueId)) {
            throw new IssueNotFoundException(issueId);
        }

        return imageRepository.findByIssueIdOrderByCreatedAtAsc(issueId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Resource getImageFile(UUID imageId) throws IOException {
        IssueImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found: " + imageId));

        return fileStorageService.getFile(image.getFilePath());
    }

    public ImageResponse getImage(UUID imageId) {
        IssueImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found: " + imageId));

        return toResponse(image);
    }

    @Transactional
    public void deleteImage(UUID imageId) throws IOException {
        IssueImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found: " + imageId));

        // Delete file
        fileStorageService.deleteFile(image.getFilePath());

        // Delete database record
        imageRepository.delete(image);

        log.info("Deleted image {} for issue {}", imageId, image.getIssueId());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed (10MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: JPEG, PNG, GIF, WebP"
            );
        }
    }

    private ImageResponse toResponse(IssueImage image) {
        return ImageResponse.builder()
                .id(image.getId())
                .issueId(image.getIssueId())
                .filename(image.getFilename())
                .originalFilename(image.getOriginalFilename())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .url(fileStorageService.getFileUrl(image.getFilePath()))
                .createdAt(image.getCreatedAt())
                .build();
    }
}
