package gov.lby.cityissuetracker.controller;

import gov.lby.cityissuetracker.dto.ImageResponse;
import gov.lby.cityissuetracker.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image upload and management for issues")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/issues/{issueId}/images")
    @Operation(
            summary = "Upload image for an issue",
            description = "Upload an image file for a specific issue. Supported formats: JPEG, PNG, GIF, WebP. Max size: 10MB."
    )
    @ApiResponse(responseCode = "201", description = "Image uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file or request")
    @ApiResponse(responseCode = "404", description = "Issue not found")
    public ResponseEntity<ImageResponse> uploadImage(
            @Parameter(description = "Issue ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID issueId,
            @Parameter(description = "Image file to upload")
            @RequestParam("file") MultipartFile file) throws IOException {

        ImageResponse response = imageService.uploadImage(issueId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/issues/{issueId}/images")
    @Operation(
            summary = "Get all images for an issue",
            description = "Retrieve metadata for all images associated with a specific issue."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved images")
    @ApiResponse(responseCode = "404", description = "Issue not found")
    public ResponseEntity<List<ImageResponse>> getImagesForIssue(
            @Parameter(description = "Issue ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID issueId) {

        List<ImageResponse> images = imageService.getImagesForIssue(issueId);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/images/{imageId}")
    @Operation(
            summary = "Get image metadata",
            description = "Retrieve metadata for a specific image."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved image metadata")
    @ApiResponse(responseCode = "404", description = "Image not found")
    public ResponseEntity<ImageResponse> getImage(
            @Parameter(description = "Image ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID imageId) {

        ImageResponse image = imageService.getImage(imageId);
        return ResponseEntity.ok(image);
    }

    @GetMapping("/images/{imageId}/file")
    @Operation(
            summary = "Download image file",
            description = "Download the actual image file."
    )
    @ApiResponse(responseCode = "200", description = "Image file returned")
    @ApiResponse(responseCode = "404", description = "Image not found")
    public ResponseEntity<Resource> downloadImage(
            @Parameter(description = "Image ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID imageId) throws IOException {

        ImageResponse metadata = imageService.getImage(imageId);
        Resource resource = imageService.getImageFile(imageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + metadata.getOriginalFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(
            summary = "Delete image",
            description = "Delete an image and its associated file."
    )
    @ApiResponse(responseCode = "204", description = "Image deleted successfully")
    @ApiResponse(responseCode = "404", description = "Image not found")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "Image ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID imageId) throws IOException {

        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}
