package com.danifgx.atomimporter;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Represents an entry in an Atom feed.
 * This class is used to parse the entries in atom files.
 */
@JacksonXmlRootElement(localName = "entry")
public class AtomEntry {

    @JacksonXmlProperty(localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "summary")
    private String summary;

    @JacksonXmlProperty(localName = "updated")
    private OffsetDateTime updated;

    @JacksonXmlProperty(localName = "link")
    private Link link;

    @JacksonXmlProperty(localName = "ContractFolderStatus", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
    private ContractFolderStatus contractFolderStatus;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public ContractFolderStatus getContractFolderStatus() {
        return contractFolderStatus;
    }

    public void setContractFolderStatus(ContractFolderStatus contractFolderStatus) {
        this.contractFolderStatus = contractFolderStatus;
    }

    /**
     * Represents a link in an Atom entry.
     */
    public static class Link {
        @JacksonXmlProperty(isAttribute = true, localName = "href")
        private String href;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }

    /**
     * Represents the contract folder status in an Atom entry.
     */
    public static class ContractFolderStatus {
        @JacksonXmlProperty(localName = "ContractFolderID", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String contractFolderId;

        @JacksonXmlProperty(localName = "ContractFolderStatusCode", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String contractFolderStatusCode;

        @JacksonXmlProperty(localName = "ProcurementProject", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
        private ProcurementProject procurementProject;

        @JacksonXmlProperty(localName = "LocatedContractingParty", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
        private LocatedContractingParty locatedContractingParty;

        public String getContractFolderId() {
            return contractFolderId;
        }

        public void setContractFolderId(String contractFolderId) {
            this.contractFolderId = contractFolderId;
        }

        public String getContractFolderStatusCode() {
            return contractFolderStatusCode;
        }

        public void setContractFolderStatusCode(String contractFolderStatusCode) {
            this.contractFolderStatusCode = contractFolderStatusCode;
        }

        public ProcurementProject getProcurementProject() {
            return procurementProject;
        }

        public void setProcurementProject(ProcurementProject procurementProject) {
            this.procurementProject = procurementProject;
        }

        public LocatedContractingParty getLocatedContractingParty() {
            return locatedContractingParty;
        }

        public void setLocatedContractingParty(LocatedContractingParty locatedContractingParty) {
            this.locatedContractingParty = locatedContractingParty;
        }
    }

    /**
     * Represents a procurement project in an Atom entry.
     */
    public static class ProcurementProject {
        @JacksonXmlProperty(localName = "TypeCode", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String typeCode;

        @JacksonXmlProperty(localName = "SubTypeCode", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String subTypeCode;

        @JacksonXmlProperty(localName = "BudgetAmount", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
        private BudgetAmount budgetAmount;

        @JacksonXmlProperty(localName = "RequiredCommodityClassification", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
        private RequiredCommodityClassification requiredCommodityClassification;

        @JacksonXmlProperty(localName = "RealizedLocation", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
        private RealizedLocation realizedLocation;

        public String getTypeCode() {
            return typeCode;
        }

        public void setTypeCode(String typeCode) {
            this.typeCode = typeCode;
        }

        public String getSubTypeCode() {
            return subTypeCode;
        }

        public void setSubTypeCode(String subTypeCode) {
            this.subTypeCode = subTypeCode;
        }

        public BudgetAmount getBudgetAmount() {
            return budgetAmount;
        }

        public void setBudgetAmount(BudgetAmount budgetAmount) {
            this.budgetAmount = budgetAmount;
        }

        public RequiredCommodityClassification getRequiredCommodityClassification() {
            return requiredCommodityClassification;
        }

        public void setRequiredCommodityClassification(RequiredCommodityClassification requiredCommodityClassification) {
            this.requiredCommodityClassification = requiredCommodityClassification;
        }

        public RealizedLocation getRealizedLocation() {
            return realizedLocation;
        }

        public void setRealizedLocation(RealizedLocation realizedLocation) {
            this.realizedLocation = realizedLocation;
        }
    }

    /**
     * Represents a budget amount in an Atom entry.
     */
    public static class BudgetAmount {
        @JacksonXmlProperty(localName = "EstimatedOverallContractAmount", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String estimatedOverallContractAmount;

        @JacksonXmlProperty(localName = "TotalAmount", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String totalAmount;

        @JacksonXmlProperty(localName = "TaxExclusiveAmount", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String taxExclusiveAmount;

        public String getEstimatedOverallContractAmount() {
            return estimatedOverallContractAmount;
        }

        public void setEstimatedOverallContractAmount(String estimatedOverallContractAmount) {
            this.estimatedOverallContractAmount = estimatedOverallContractAmount;
        }

        public String getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(String totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getTaxExclusiveAmount() {
            return taxExclusiveAmount;
        }

        public void setTaxExclusiveAmount(String taxExclusiveAmount) {
            this.taxExclusiveAmount = taxExclusiveAmount;
        }
    }

    /**
     * Represents a required commodity classification in an Atom entry.
     */
    public static class RequiredCommodityClassification {
        @JacksonXmlProperty(localName = "ItemClassificationCode", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String itemClassificationCode;

        public String getItemClassificationCode() {
            return itemClassificationCode;
        }

        public void setItemClassificationCode(String itemClassificationCode) {
            this.itemClassificationCode = itemClassificationCode;
        }
    }

    /**
     * Represents a realized location in an Atom entry.
     */
    public static class RealizedLocation {
        @JacksonXmlProperty(localName = "CountrySubentity", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String countrySubentity;

        @JacksonXmlProperty(localName = "CountrySubentityCode", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String countrySubentityCode;

        public String getCountrySubentity() {
            return countrySubentity;
        }

        public void setCountrySubentity(String countrySubentity) {
            this.countrySubentity = countrySubentity;
        }

        public String getCountrySubentityCode() {
            return countrySubentityCode;
        }

        public void setCountrySubentityCode(String countrySubentityCode) {
            this.countrySubentityCode = countrySubentityCode;
        }
    }

    /**
     * Represents a located contracting party in an Atom entry.
     */
    public static class LocatedContractingParty {
        @JacksonXmlProperty(localName = "Party", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
        private Party party;

        public Party getParty() {
            return party;
        }

        public void setParty(Party party) {
            this.party = party;
        }
    }

    /**
     * Represents a party in an Atom entry.
     */
    public static class Party {
        @JacksonXmlProperty(localName = "PartyName", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
        private PartyName partyName;

        @JacksonXmlProperty(localName = "PartyIdentification", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2")
        private PartyIdentification partyIdentification;

        public PartyName getPartyName() {
            return partyName;
        }

        public void setPartyName(PartyName partyName) {
            this.partyName = partyName;
        }

        public PartyIdentification getPartyIdentification() {
            return partyIdentification;
        }

        public void setPartyIdentification(PartyIdentification partyIdentification) {
            this.partyIdentification = partyIdentification;
        }
    }

    /**
     * Represents a party name in an Atom entry.
     */
    public static class PartyName {
        @JacksonXmlProperty(localName = "Name", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Represents a party identification in an Atom entry.
     */
    public static class PartyIdentification {
        @JacksonXmlProperty(localName = "ID", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
