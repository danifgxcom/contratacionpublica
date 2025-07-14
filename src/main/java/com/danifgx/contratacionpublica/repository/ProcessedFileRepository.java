package com.danifgx.contratacionpublica.repository;

import com.danifgx.contratacionpublica.model.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ProcessedFile entity.
 */
@Repository
public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, UUID> {

    /**
     * Find a processed file by its file name.
     *
     * @param fileName the name of the file
     * @return the processed file if found
     */
    Optional<ProcessedFile> findByFileName(String fileName);

    /**
     * Check if a file with the given name has been processed.
     *
     * @param fileName the name of the file
     * @return true if the file has been processed
     */
    boolean existsByFileName(String fileName);

    /**
     * Check if a file with the given path has been processed.
     *
     * @param filePath the path of the file
     * @return true if the file has been processed
     */
    boolean existsByFilePath(String filePath);
}
