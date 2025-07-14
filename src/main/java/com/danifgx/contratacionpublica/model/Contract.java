package com.danifgx.contratacionpublica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a public procurement contract.
 * This is the main entity that stores information about contracts from the .atom files.
 */
@Entity
@Table(name = "contracts", indexes = {
    @Index(name = "idx_organismo", columnList = "contracting_party_name"),
    @Index(name = "idx_fecha", columnList = "updated_at")
    // Note: GIN index with to_tsvector cannot be defined via JPA annotations
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Original ID from the .atom file
     */
    @Column(name = "external_id", unique = true, nullable = false)
    private String externalId;

    /**
     * Contract folder ID (e.g., "72/2025")
     */
    @Column(name = "folder_id")
    private String folderId;

    /**
     * Title of the contract
     */
    @Column(name = "title", length = 1000)
    private String title;

    /**
     * Summary of the contract
     */
    @Column(name = "summary", length = 2000)
    private String summary;

    /**
     * Status of the contract (e.g., "PUB" for published)
     */
    @Column(name = "status")
    private String status;

    /**
     * Last update date of the contract
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Link to the contract details
     */
    @Column(name = "link", length = 500)
    private String link;

    /**
     * Estimated amount of the contract (excluding taxes)
     */
    @Column(name = "estimated_amount")
    private Double estimatedAmount;

    /**
     * Total amount of the contract (including taxes)
     */
    @Column(name = "total_amount")
    private Double totalAmount;

    /**
     * Tax exclusive amount of the contract
     */
    @Column(name = "tax_exclusive_amount")
    private Double taxExclusiveAmount;

    /**
     * Currency of the amounts (e.g., "EUR")
     */
    @Column(name = "currency")
    private String currency;

    /**
     * Type of contract (e.g., "2" for services)
     */
    @Column(name = "type_code")
    private String typeCode;

    /**
     * Subtype of contract (e.g., "14" for cleaning services)
     */
    @Column(name = "subtype_code")
    private String subtypeCode;

    /**
     * CPV code (Common Procurement Vocabulary)
     */
    @Column(name = "cpv_code")
    private String cpvCode;

    /**
     * Location of the contract (NUTS code)
     */
    @Column(name = "nuts_code")
    private String nutsCode;

    /**
     * Location of the contract (country subentity)
     */
    @Column(name = "country_subentity")
    private String countrySubentity;

    /**
     * Name of the contracting party
     */
    @Column(name = "contracting_party_name", length = 500)
    private String contractingPartyName;

    /**
     * ID of the contracting party (e.g., NIF)
     */
    @Column(name = "contracting_party_id")
    private String contractingPartyId;

    /**
     * Original file name from which this contract was imported
     */
    @Column(name = "source_file", length = 500)
    private String sourceFile;

    /**
     * Source of the contract data (perfiles or agregadas)
     */
    @Column(name = "source")
    private String source;

    /**
     * Date when the contract was imported
     */
    @Column(name = "imported_at")
    private LocalDateTime importedAt;
}
