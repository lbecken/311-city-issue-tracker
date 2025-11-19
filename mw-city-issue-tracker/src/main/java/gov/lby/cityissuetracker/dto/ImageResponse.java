package gov.lby.cityissuetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private UUID id;
    private UUID issueId;
    private String filename;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String url;
    private LocalDateTime createdAt;
}
