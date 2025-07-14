package com.danifgx.contratacionpublica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a processed file.
 * This is used to keep track of which files have been processed to avoid processing the same file twice.
 */
@Entity
@Table(name = "processed_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The name of the processed file
     */
    @Column(name = "file_name", unique = true, nullable = false)
    private String fileName;

    /**
     * The path of the processed file
     */
    @Column(name = "file_path", length = 1000)
    private String filePath;

    /**
     * The number of contracts processed from this file
     */
    @Column(name = "contracts_processed")
    private Integer contractsProcessed;

    /**
     * The number of contracts that were duplicates and not processed
     */
    @Column(name = "duplicates_found")
    private Integer duplicatesFound;

    /**
     * The date when the file was processed
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * The status of the processing (e.g., "COMPLETED", "FAILED")
     */
    @Column(name = "status")
    private String status;

    /**
     * Any error message if the processing failed
     */
    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}