package gov.lby.cityissuetracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * S3 implementation of FileStorageService.
 * This is a stub for future implementation when moving to production.
 *
 * To implement:
 * 1. Add AWS SDK dependency
 * 2. Configure S3 bucket and credentials
 * 3. Implement the methods using S3Client
 */
@Service
@Profile("production")
@Slf4j
public class S3FileStorageService implements FileStorageService {

    // TODO: Inject S3Client when implementing
    // private final S3Client s3Client;
    // private final S3Presigner presigner;
    // @Value("${aws.s3.bucket}") private String bucketName;

    @Override
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        // TODO: Implement S3 upload
        // String key = "issues/" + subDirectory + "/" + UUID.randomUUID() + extension;
        // s3Client.putObject(PutObjectRequest.builder()
        //     .bucket(bucketName)
        //     .key(key)
        //     .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        // return key;
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public Resource getFile(String filePath) throws IOException {
        // TODO: Implement S3 download
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        // TODO: Implement S3 delete
        // s3Client.deleteObject(DeleteObjectRequest.builder()
        //     .bucket(bucketName)
        //     .key(filePath)
        //     .build());
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public boolean fileExists(String filePath) {
        // TODO: Implement S3 exists check
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public String getFileUrl(String filePath) {
        // TODO: Return CloudFront or S3 URL
        // For presigned URLs:
        // GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        //     .signatureDuration(Duration.ofHours(1))
        //     .getObjectRequest(GetObjectRequest.builder()
        //         .bucket(bucketName)
        //         .key(filePath)
        //         .build())
        //     .build();
        // return presigner.presignGetObject(presignRequest).url().toString();
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }
}
