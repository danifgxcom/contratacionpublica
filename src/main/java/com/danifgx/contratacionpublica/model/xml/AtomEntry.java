package com.danifgx.contratacionpublica.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Class representing an entry in an Atom feed.
 * This is used for parsing the .atom files.
 */
@Data
public class AtomEntry {

    @JacksonXmlProperty(localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "link")
    private AtomLink link;

    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "summary")
    private String summary;

    @JacksonXmlProperty(localName = "updated")
    private LocalDateTime updated;

    @JacksonXmlProperty(localName = "ContractFolderStatus", namespace = "urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonAggregateComponents-2")
    private ContractFolderStatus contractFolderStatus;

    /**
     * Class representing a link in an Atom entry.
     */
    @Data
    public static class AtomLink {
        @JacksonXmlProperty(isAttribute = true, localName = "href")
        private String href;
    }

    /**
     * Class representing the contract folder status in an Atom entry.
     */
    @Data
    public static class ContractFolderStatus {
        @JacksonXmlProperty(localName = "ContractFolderID", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String contractFolderId;

        @JacksonXmlProperty(localName = "ContractFolderStatusCode", namespace = "urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonBasicComponents-2")
        private String contractFolderStatusCode;

        @JacksonXmlProperty(localName = "UUID", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String uuid;

        @JacksonXmlProperty(localName = "LocatedContractingParty", namespace = "urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonAggregateComponents-2")
        private LocatedContractingParty locatedContractingParty;

        @JacksonXmlProperty(localName = "ProcurementProject", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2")
        private ProcurementProject procurementProject;
    }

    /**
     * Class representing the located contracting party in a contract folder status.
     */
    @Data
    public static class LocatedContractingParty {
        @JacksonXmlProperty(localName = "Party", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2")
        private Party party;
    }

    /**
     * Class representing a party in a located contracting party.
     */
    @Data
    public static class Party {
        @JacksonXmlProperty(localName = "PartyIdentification", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2")
        private PartyIdentification partyIdentification;

        @JacksonXmlProperty(localName = "PartyName", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2")
        private PartyName partyName;
    }

    /**
     * Class representing a party identification in a party.
     */
    @Data
    public static class PartyIdentification {
        @JacksonXmlProperty(localName = "ID", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String id;
    }

    /**
     * Class representing a party name in a party.
     */
    @Data
    public static class PartyName {
        @JacksonXmlProperty(localName = "Name", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String name;
    }

    /**
     * Class representing a procurement project in a contract folder status.
     */
    @Data
    public static class ProcurementProject {
        @JacksonXmlProperty(localName = "Name", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String name;

        @JacksonXmlProperty(localName = "TypeCode", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String typeCode;

        @JacksonXmlProperty(localName = "SubTypeCode", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String subTypeCode;

        @JacksonXmlProperty(localName = "BudgetAmount", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2")
        private BudgetAmount budgetAmount;

        @JacksonXmlProperty(localName = "RequiredCommodityClassification", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2")
        private RequiredCommodityClassification requiredCommodityClassification;

        @JacksonXmlProperty(localName = "RealizedLocation", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2")
        private RealizedLocation realizedLocation;
    }

    /**
     * Class representing a budget amount in a procurement project.
     */
    @Data
    public static class BudgetAmount {
        @JacksonXmlProperty(localName = "EstimatedOverallContractAmount", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String estimatedOverallContractAmount;

        @JacksonXmlProperty(localName = "TotalAmount", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String totalAmount;

        @JacksonXmlProperty(localName = "TaxExclusiveAmount", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String taxExclusiveAmount;
    }

    /**
     * Class representing a required commodity classification in a procurement project.
     */
    @Data
    public static class RequiredCommodityClassification {
        @JacksonXmlProperty(localName = "ItemClassificationCode", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String itemClassificationCode;
    }

    /**
     * Class representing a realized location in a procurement project.
     */
    @Data
    public static class RealizedLocation {
        @JacksonXmlProperty(localName = "CountrySubentity", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String countrySubentity;

        @JacksonXmlProperty(localName = "CountrySubentityCode", namespace = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2")
        private String countrySubentityCode;
    }
}
