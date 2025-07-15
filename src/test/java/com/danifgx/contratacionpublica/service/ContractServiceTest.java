package com.danifgx.contratacionpublica.service;

import com.danifgx.contratacionpublica.model.Contract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the ContractService Facade class.
 */
@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractQueryService contractQueryService;

    @Mock
    private ContractSearchService contractSearchService;

    @Mock
    private ContractStatisticsService contractStatisticsService;

    @InjectMocks
    private ContractService contractService;

    private Contract testContract;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testContract = new Contract();
        testContract.setId(testId);
        testContract.setTitle("Test Contract");
    }

    // Test Query Operations Delegation
    @Test
    void getContractById_ShouldDelegateToQueryService() {
        // Given
        when(contractQueryService.findById(testId)).thenReturn(Optional.of(testContract));

        // When
        Optional<Contract> result = contractService.getContractById(testId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testContract, result.get());
        verify(contractQueryService).findById(testId);
        verifyNoInteractions(contractSearchService, contractStatisticsService);
    }

    @Test
    void getContractByExternalId_ShouldDelegateToQueryService() {
        // Given
        String externalId = "EXT-123";
        when(contractQueryService.findByExternalId(externalId)).thenReturn(Optional.of(testContract));

        // When
        Optional<Contract> result = contractService.getContractByExternalId(externalId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testContract, result.get());
        verify(contractQueryService).findByExternalId(externalId);
        verifyNoInteractions(contractSearchService, contractStatisticsService);
    }

    @Test
    void getAllContracts_ShouldDelegateToQueryService() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contract> expectedPage = new PageImpl<>(List.of(testContract));
        when(contractQueryService.findAll(pageable)).thenReturn(expectedPage);

        // When
        Page<Contract> result = contractService.getAllContracts(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(contractQueryService).findAll(pageable);
        verifyNoInteractions(contractSearchService, contractStatisticsService);
    }

    @Test
    void getContractCount_ShouldDelegateToQueryService() {
        // Given
        when(contractQueryService.count()).thenReturn(42L);

        // When
        Long result = contractService.getContractCount();

        // Then
        assertEquals(42L, result);
        verify(contractQueryService).count();
        verifyNoInteractions(contractSearchService, contractStatisticsService);
    }

    // Test Search Operations Delegation
    @Test
    void searchContractsByTitle_ShouldDelegateToSearchService() {
        // Given
        String title = "Test";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contract> expectedPage = new PageImpl<>(List.of(testContract));
        when(contractSearchService.searchByTitle(title, pageable)).thenReturn(expectedPage);

        // When
        Page<Contract> result = contractService.searchContractsByTitle(title, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(contractSearchService).searchByTitle(title, pageable);
        verifyNoInteractions(contractQueryService, contractStatisticsService);
    }

    @Test
    void getContractingPartyAutocomplete_ShouldDelegateToSearchService() {
        // Given
        String query = "Test";
        List<String> expectedSuggestions = List.of("Test Organization");
        when(contractSearchService.getContractingPartyAutocomplete(query)).thenReturn(expectedSuggestions);

        // When
        List<String> result = contractService.getContractingPartyAutocomplete(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Organization", result.get(0));
        verify(contractSearchService).getContractingPartyAutocomplete(query);
        verifyNoInteractions(contractQueryService, contractStatisticsService);
    }

    // Test Statistics Operations Delegation
    @Test
    void getContractStatistics_ShouldDelegateToStatisticsService() {
        // Given
        Map<String, Object> expectedStats = Map.of("totalContracts", 100L);
        when(contractStatisticsService.getComprehensiveStatistics()).thenReturn(expectedStats);

        // When
        Map<String, Object> result = contractService.getContractStatistics();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.get("totalContracts"));
        verify(contractStatisticsService).getComprehensiveStatistics();
        verifyNoInteractions(contractQueryService, contractSearchService);
    }

    @Test
    void getStatisticsByAutonomousCommunity_ShouldDelegateToStatisticsService() {
        // Given
        List<Map<String, Object>> expectedStats = List.of(
                Map.of("name", "Madrid", "contractCount", 40L)
        );
        when(contractStatisticsService.getStatisticsByAutonomousCommunity()).thenReturn(expectedStats);

        // When
        List<Map<String, Object>> result = contractService.getStatisticsByAutonomousCommunity();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Madrid", result.get(0).get("name"));
        verify(contractStatisticsService).getStatisticsByAutonomousCommunity();
        verifyNoInteractions(contractQueryService, contractSearchService);
    }

    @Test
    void getDistinctYears_ShouldDelegateToStatisticsService() {
        // Given
        List<Integer> expectedYears = List.of(2023, 2022, 2021);
        when(contractStatisticsService.getDistinctYears()).thenReturn(expectedYears);

        // When
        List<Integer> result = contractService.getDistinctYears();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedYears, result);
        verify(contractStatisticsService).getDistinctYears();
        verifyNoInteractions(contractQueryService, contractSearchService);
    }

    @Test
    void getDistinctRegions_ShouldDelegateToStatisticsService() {
        // Given
        Map<String, String> expectedRegions = Map.of("ES30", "Madrid", "ES51", "Cataluña");
        when(contractStatisticsService.getDistinctRegions()).thenReturn(expectedRegions);

        // When
        Map<String, String> result = contractService.getDistinctRegions();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Madrid", result.get("ES30"));
        assertEquals("Cataluña", result.get("ES51"));
        verify(contractStatisticsService).getDistinctRegions();
        verifyNoInteractions(contractQueryService, contractSearchService);
    }
}
