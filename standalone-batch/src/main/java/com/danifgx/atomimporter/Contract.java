package com.danifgx.atomimporter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a contract entity.
 * This is a simplified version of the Contract entity from the Spring Boot application.
 */
public class Contract {
    private UUID id;
    private String externalId;
    private String title;
    private String summary;
    private LocalDateTime updatedAt;
    private LocalDateTime importedAt;
    private String link;
    private String sourceFile;
    private String source;
    private String folderId;
    private String status;
    private String typeCode;
    private String subtypeCode;
    private Double estimatedAmount;
    private Double totalAmount;
    private Double taxExclusiveAmount;
    private String currency;
    private String cpvCode;
    private String countrySubentity;
    private String nutsCode;
    private String contractingPartyName;
    private String contractingPartyId;

    public Contract() {
        this.id = UUID.randomUUID();
        this.importedAt = LocalDateTime.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getSubtypeCode() {
        return subtypeCode;
    }

    public void setSubtypeCode(String subtypeCode) {
        this.subtypeCode = subtypeCode;
    }

    public Double getEstimatedAmount() {
        return estimatedAmount;
    }

    public void setEstimatedAmount(Double estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getTaxExclusiveAmount() {
        return taxExclusiveAmount;
    }

    public void setTaxExclusiveAmount(Double taxExclusiveAmount) {
        this.taxExclusiveAmount = taxExclusiveAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCpvCode() {
        return cpvCode;
    }

    public void setCpvCode(String cpvCode) {
        this.cpvCode = cpvCode;
    }

    public String getCountrySubentity() {
        return countrySubentity;
    }

    public void setCountrySubentity(String countrySubentity) {
        this.countrySubentity = countrySubentity;
    }

    public String getNutsCode() {
        return nutsCode;
    }

    public void setNutsCode(String nutsCode) {
        this.nutsCode = nutsCode;
    }

    public String getContractingPartyName() {
        return contractingPartyName;
    }

    public void setContractingPartyName(String contractingPartyName) {
        this.contractingPartyName = contractingPartyName;
    }

    public String getContractingPartyId() {
        return contractingPartyId;
    }

    public void setContractingPartyId(String contractingPartyId) {
        this.contractingPartyId = contractingPartyId;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", externalId='" + externalId + '\'' +
                ", title='" + title + '\'' +
                ", updatedAt=" + updatedAt +
                ", importedAt=" + importedAt +
                ", sourceFile='" + sourceFile + '\'' +
                '}';
    }
}
