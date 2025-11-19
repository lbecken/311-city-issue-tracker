package gov.lby.cityissuetracker.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interface for file storage operations.
 * Implementations can store files locally or in cloud storage (S3, etc.)
 */
public interface FileStorageService {

    /**
     * Store a file and return the path/key where it was stored.
     *
     * @param file The file to store
     * @param subDirectory Optional subdirectory for organization
     * @return The path/key where the file was stored
     * @throws IOException if storage fails
     */
    String storeFile(MultipartFile file, String subDirectory) throws IOException;

    /**
     * Retrieve a file as a Resource.
     *
     * @param filePath The path/key of the file
     * @return The file as a Resource
     * @throws IOException if retrieval fails
     */
    Resource getFile(String filePath) throws IOException;

    /**
     * Delete a file.
     *
     * @param filePath The path/key of the file
     * @throws IOException if deletion fails
     */
    void deleteFile(String filePath) throws IOException;

    /**
     * Check if a file exists.
     *
     * @param filePath The path/key of the file
     * @return true if file exists
     */
    boolean fileExists(String filePath);

    /**
     * Get the public URL for accessing the file.
     *
     * @param filePath The path/key of the file
     * @return The public URL
     */
    String getFileUrl(String filePath);
}
