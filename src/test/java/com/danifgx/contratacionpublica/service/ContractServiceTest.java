package com.danifgx.contratacionpublica.service;

import com.danifgx.contratacionpublica.model.Contract;
import com.danifgx.contratacionpublica.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Tests for the ContractService class.
 */
public class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractTypeService contractTypeService;

    @InjectMocks
    private ContractService contractService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test that the getContractStatistics method handles null values correctly.
     */
    @Test
    public void testGetContractStatistics_HandlesNullValues() {
        // Mock data with null values
        List<Object[]> typeCodeStats = Arrays.asList(
                new Object[]{"1", 10L},
                new Object[]{"2", 20L},
                new Object[]{"Unknown", 5L}  // This represents null values that were replaced with "Unknown"
        );

        List<Object[]> statusStats = Arrays.asList(
                new Object[]{"PUB", 15L},
                new Object[]{"EV", 10L},
                new Object[]{"Unknown", 10L}  // This represents null values that were replaced with "Unknown"
        );

        List<Object[]> sourceStats = Arrays.asList(
                new Object[]{"perfiles", 20L},
                new Object[]{"agregadas", 15L},
                new Object[]{"Unknown", 5L}  // This represents null values that were replaced with "Unknown"
        );

        // Mock data for top contracting organizations, including a case with null totalAmount
        List<Object[]> topOrganizations = Arrays.asList(
                new Object[]{"Organization 1", 10L, 1000000.0},
                new Object[]{"Organization 2", 8L, 800000.0},
                new Object[]{"Organization 3", 5L, null}  // This organization has null totalAmount
        );

        // Mock data for autonomous communities statistics
        List<Object[]> autonomousCommunityStats = Arrays.asList(
                new Object[]{"Madrid", 25L, 2500000.0, 100000.0},
                new Object[]{"Cataluña", 20L, 2000000.0, 100000.0},
                new Object[]{"Andalucía", 15L, 1500000.0, 100000.0},
                new Object[]{"Unknown", 5L, 0.0, 0.0}  // Unknown autonomous community
        );

        // Mock repository methods
        when(contractRepository.count()).thenReturn(40L);
        when(contractRepository.countByTypeCode()).thenReturn(typeCodeStats);
        when(contractRepository.countByStatus()).thenReturn(statusStats);
        when(contractRepository.countBySource()).thenReturn(sourceStats);
        when(contractRepository.findTopContractingOrganizations()).thenReturn(topOrganizations);
        when(contractRepository.countByAutonomousCommunity()).thenReturn(autonomousCommunityStats);

        // Mock ContractTypeService behavior
        when(contractTypeService.getDescriptionForCode("1")).thenReturn("Type 1 Description");
        when(contractTypeService.getDescriptionForCode("2")).thenReturn("Type 2 Description");
        when(contractTypeService.getDescriptionForCode("Unknown")).thenReturn("Unknown Type");

        // Call the service method
        Map<String, Object> statistics = contractService.getContractStatistics();

        // Verify the results
        System.out.println("[DEBUG_LOG] Statistics: " + statistics);

        assertNotNull(statistics);
        assertEquals(40L, statistics.get("totalContracts"));

        Map<String, Map<String, Object>> countByTypeCode = (Map<String, Map<String, Object>>) statistics.get("countByTypeCode");
        assertNotNull(countByTypeCode);
        assertEquals(3, countByTypeCode.size());

        // Check type code 1
        Map<String, Object> type1 = countByTypeCode.get("1");
        assertNotNull(type1);
        assertEquals(10L, type1.get("count"));
        assertEquals("Type 1 Description", type1.get("description"));

        // Check type code 2
        Map<String, Object> type2 = countByTypeCode.get("2");
        assertNotNull(type2);
        assertEquals(20L, type2.get("count"));
        assertEquals("Type 2 Description", type2.get("description"));

        // Check Unknown type code
        Map<String, Object> typeUnknown = countByTypeCode.get("Unknown");
        assertNotNull(typeUnknown);
        assertEquals(5L, typeUnknown.get("count"));
        assertEquals("Unknown Type", typeUnknown.get("description"));

        Map<String, Long> countByStatus = (Map<String, Long>) statistics.get("countByStatus");
        assertNotNull(countByStatus);
        assertEquals(3, countByStatus.size());
        assertEquals(15L, countByStatus.get("PUB"));
        assertEquals(10L, countByStatus.get("EV"));
        assertEquals(10L, countByStatus.get("Unknown"));

        Map<String, Long> countBySource = (Map<String, Long>) statistics.get("countBySource");
        assertNotNull(countBySource);
        assertEquals(3, countBySource.size());
        assertEquals(20L, countBySource.get("perfiles"));
        assertEquals(15L, countBySource.get("agregadas"));
        assertEquals(5L, countBySource.get("Unknown"));

        // Verify topOrganizations, including the case with null totalAmount
        List<Map<String, Object>> topOrgs = (List<Map<String, Object>>) statistics.get("topOrganizations");
        assertNotNull(topOrgs);
        assertEquals(3, topOrgs.size());

        // Check first organization
        assertEquals("Organization 1", topOrgs.get(0).get("name"));
        assertEquals(10L, topOrgs.get(0).get("contractCount"));
        assertEquals(1000000.0, topOrgs.get(0).get("totalAmount"));

        // Check third organization with null totalAmount
        assertEquals("Organization 3", topOrgs.get(2).get("name"));
        assertEquals(5L, topOrgs.get(2).get("contractCount"));
        assertEquals(null, topOrgs.get(2).get("totalAmount"));

        // Verify autonomous communities statistics
        List<Map<String, Object>> autonomousCommunities = (List<Map<String, Object>>) statistics.get("countByAutonomousCommunity");
        assertNotNull(autonomousCommunities);
        assertEquals(4, autonomousCommunities.size());

        // Check Madrid
        Map<String, Object> madrid = autonomousCommunities.get(0);
        assertEquals("Madrid", madrid.get("name"));
        assertEquals(25L, madrid.get("contractCount"));
        assertEquals(2500000.0, madrid.get("totalAmount"));
        assertEquals(100000.0, madrid.get("averageAmount"));

        // Check Cataluña
        Map<String, Object> cataluna = autonomousCommunities.get(1);
        assertEquals("Cataluña", cataluna.get("name"));
        assertEquals(20L, cataluna.get("contractCount"));
        assertEquals(2000000.0, cataluna.get("totalAmount"));
        assertEquals(100000.0, cataluna.get("averageAmount"));

        // Check Unknown autonomous community
        Map<String, Object> unknown = autonomousCommunities.get(3);
        assertEquals("Unknown", unknown.get("name"));
        assertEquals(5L, unknown.get("contractCount"));
        assertEquals(0.0, unknown.get("totalAmount"));
        assertEquals(0.0, unknown.get("averageAmount"));
    }
}
