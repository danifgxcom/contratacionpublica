package com.danifgx.contratacionpublica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a contract type.
 * This entity maps contract type codes to their human-readable descriptions.
 */
@Entity
@Table(name = "contract_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractType {

    @Id
    @Column(name = "code", length = 10)
    private String code;

    /**
     * Human-readable description of the contract type
     */
    @Column(name = "description", length = 100, nullable = false)
    private String description;

    /**
     * Flag indicating whether this is a known contract type
     */
    @Column(name = "is_known", nullable = false)
    private boolean known;
}