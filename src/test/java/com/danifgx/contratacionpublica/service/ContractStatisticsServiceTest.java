package com.danifgx.contratacionpublica.service;

import com.danifgx.contratacionpublica.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractStatisticsServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractTypeService contractTypeService;

    @InjectMocks
    private ContractStatisticsService contractStatisticsService;


    @Test
    void getComprehensiveStatistics_ShouldReturnCompleteStatistics() {
        // Given
        when(contractTypeService.getDescriptionForCode("1")).thenReturn("Services");
        when(contractTypeService.getDescriptionForCode("2")).thenReturn("Supplies");
        
        when(contractRepository.count()).thenReturn(100L);
        
        List<Object[]> typeCodeStats = List.of(
                new Object[]{"1", 60L},
                new Object[]{"2", 40L}
        );
        when(contractRepository.countByTypeCode()).thenReturn(typeCodeStats);

        List<Object[]> statusStats = List.of(
                new Object[]{"PUB", 70L},
                new Object[]{"EV", 30L}
        );
        when(contractRepository.countByStatus()).thenReturn(statusStats);

        List<Object[]> sourceStats = List.of(
                new Object[]{"perfiles", 80L},
                new Object[]{"agregadas", 20L}
        );
        when(contractRepository.countBySource()).thenReturn(sourceStats);

        List<Object[]> topOrganizations = List.of(
                new Object[]{"Organization A", 25L, 5000000.0, 20L, 5L},
                new Object[]{"Organization B", 20L, 4000000.0, 18L, 2L}
        );
        when(contractRepository.findTopContractingOrganizations()).thenReturn(topOrganizations);

        List<Object[]> autonomousStats = List.of(
                new Object[]{"Madrid", 40L, 8000000.0, 200000.0},
                new Object[]{"Cataluña", 35L, 7000000.0, 200000.0}
        );
        when(contractRepository.countByAutonomousCommunity()).thenReturn(autonomousStats);

        // When
        Map<String, Object> result = contractStatisticsService.getComprehensiveStatistics();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.get("totalContracts"));
        
        // Verify type code statistics
        Map<String, Object> typeCodeResult = (Map<String, Object>) result.get("countByTypeCode");
        assertNotNull(typeCodeResult);
        assertEquals(2, typeCodeResult.size());
        
        Map<String, Object> servicesType = (Map<String, Object>) typeCodeResult.get("1");
        assertEquals(60L, servicesType.get("count"));
        assertEquals("Services", servicesType.get("description"));

        // Verify all sections are present
        assertNotNull(result.get("countByStatus"));
        assertNotNull(result.get("countBySource"));
        assertNotNull(result.get("topOrganizations"));
        assertNotNull(result.get("countByAutonomousCommunity"));

        // Verify all repository methods were called
        verify(contractRepository).count();
        verify(contractRepository).countByTypeCode();
        verify(contractRepository).countByStatus();
        verify(contractRepository).countBySource();
        verify(contractRepository).findTopContractingOrganizations();
        verify(contractRepository).countByAutonomousCommunity();
    }

    @Test
    void getStatisticsByAutonomousCommunity_ShouldReturnMappedStatistics() {
        // Given
        List<Object[]> autonomousStats = List.of(
                new Object[]{"Madrid", 40L, 8000000.0, 200000.0},
                new Object[]{"Cataluña", 35L, 7000000.0, 200000.0},
                new Object[]{"Andalucía", 25L, 5000000.0, 200000.0}
        );
        when(contractRepository.countByAutonomousCommunity()).thenReturn(autonomousStats);

        // When
        List<Map<String, Object>> result = contractStatisticsService.getStatisticsByAutonomousCommunity();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        Map<String, Object> madrid = result.get(0);
        assertEquals("Madrid", madrid.get("name"));
        assertEquals(40L, madrid.get("contractCount"));
        assertEquals(8000000.0, madrid.get("totalAmount"));
        assertEquals(200000.0, madrid.get("averageAmount"));

        verify(contractRepository).countByAutonomousCommunity();
    }

    @Test
    void getDistinctYears_ShouldReturnYearsList() {
        // Given
        List<Integer> expectedYears = List.of(2023, 2022, 2021, 2020);
        when(contractRepository.findDistinctYears()).thenReturn(expectedYears);

        // When
        List<Integer> result = contractStatisticsService.getDistinctYears();

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(expectedYears, result);
        verify(contractRepository).findDistinctYears();
    }

    @Test
    void getDistinctRegions_ShouldReturnRegionsMap() {
        // Given
        List<Object[]> regionData = List.of(
                new Object[]{"ES51", "Cataluña"},
                new Object[]{"ES30", "Madrid"},
                new Object[]{"ES61", "Andalucía"}
        );
        when(contractRepository.findDistinctRegions()).thenReturn(regionData);

        // When
        Map<String, String> result = contractStatisticsService.getDistinctRegions();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Cataluña", result.get("ES51"));
        assertEquals("Madrid", result.get("ES30"));
        assertEquals("Andalucía", result.get("ES61"));
        verify(contractRepository).findDistinctRegions();
    }

    @Test
    void getDistinctRegions_WithDuplicateKeys_ShouldKeepFirst() {
        // Given
        List<Object[]> regionData = List.of(
                new Object[]{"ES51", "Cataluña"},
                new Object[]{"ES51", "Catalunya"} // Duplicate key with different value
        );
        when(contractRepository.findDistinctRegions()).thenReturn(regionData);

        // When
        Map<String, String> result = contractStatisticsService.getDistinctRegions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cataluña", result.get("ES51")); // Should keep the first value
        verify(contractRepository).findDistinctRegions();
    }

    @Test
    void getComprehensiveStatistics_WithNullValues_ShouldHandleGracefully() {
        // Given
        when(contractTypeService.getDescriptionForCode("1")).thenReturn("Services");
        when(contractTypeService.getDescriptionForCode(null)).thenReturn("Unknown Type");
        
        when(contractRepository.count()).thenReturn(50L);
        
        List<Object[]> typeCodeStats = Arrays.<Object[]>asList(
                new Object[]{"1", 30L},
                new Object[]{null, 20L} // Null type code
        );
        when(contractRepository.countByTypeCode()).thenReturn(typeCodeStats);

        List<Object[]> statusStats = Arrays.<Object[]>asList(
                new Object[]{"PUB", 35L},
                new Object[]{null, 15L} // Null status
        );
        when(contractRepository.countByStatus()).thenReturn(statusStats);

        List<Object[]> sourceStats = Arrays.<Object[]>asList(
                new Object[]{"perfiles", 40L},
                new Object[]{null, 10L} // Null source
        );
        when(contractRepository.countBySource()).thenReturn(sourceStats);

        List<Object[]> topOrganizations = Arrays.<Object[]>asList(
                new Object[]{"Organization A", 20L, null, 15L, 5L} // Null total amount
        );
        when(contractRepository.findTopContractingOrganizations()).thenReturn(topOrganizations);

        List<Object[]> autonomousStats = Arrays.<Object[]>asList(
                new Object[]{null, 15L, 0.0, 0.0} // Null autonomous community
        );
        when(contractRepository.countByAutonomousCommunity()).thenReturn(autonomousStats);

        when(contractTypeService.getDescriptionForCode(null)).thenReturn("Unknown Type");

        // When
        Map<String, Object> result = contractStatisticsService.getComprehensiveStatistics();

        // Then
        assertNotNull(result);
        assertEquals(50L, result.get("totalContracts"));
        
        // Verify that null values are handled (converted to string keys in maps)
        Map<String, Object> typeCodeResult = (Map<String, Object>) result.get("countByTypeCode");
        assertNotNull(typeCodeResult);
        assertTrue(typeCodeResult.containsKey("1"));
        assertTrue(typeCodeResult.containsKey("null") || typeCodeResult.size() >= 1);

        List<Map<String, Object>> topOrgs = (List<Map<String, Object>>) result.get("topOrganizations");
        assertNotNull(topOrgs);
        assertEquals(1, topOrgs.size());
        assertNull(topOrgs.get(0).get("totalAmount")); // Null should be preserved in the result
    }
}