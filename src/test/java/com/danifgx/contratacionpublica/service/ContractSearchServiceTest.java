package com.danifgx.contratacionpublica.service;

import com.danifgx.contratacionpublica.model.Contract;
import com.danifgx.contratacionpublica.repository.ContractRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractSearchServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private ContractSearchService contractSearchService;

    private Contract testContract;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testContract = new Contract();
        testContract.setTitle("Test Contract");
        testContract.setContractingPartyName("Test Organization");
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void searchByTitle_WithValidTitle_ShouldReturnContracts() {
        // Given
        String title = "Test";
        Page<Contract> expectedPage = new PageImpl<>(List.of(testContract));
        when(contractRepository.findByTitleContainingIgnoreCase(title, pageable)).thenReturn(expectedPage);

        // When
        Page<Contract> result = contractSearchService.searchByTitle(title, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(contractRepository).findByTitleContainingIgnoreCase(title, pageable);
    }

    @Test
    void searchByTitle_WithNullTitle_ShouldThrowException() {
        // Given
        String title = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> contractSearchService.searchByTitle(title, pageable));
        assertEquals("title cannot be null or empty", exception.getMessage());
        verifyNoInteractions(contractRepository);
    }

    @Test
    void searchByTitle_WithEmptyTitle_ShouldThrowException() {
        // Given
        String title = "";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> contractSearchService.searchByTitle(title, pageable));
        assertEquals("title cannot be null or empty", exception.getMessage());
        verifyNoInteractions(contractRepository);
    }

    @Test
    void searchByContractingPartyName_WithValidName_ShouldReturnContracts() {
        // Given
        String partyName = "Test Organization";
        Page<Contract> expectedPage = new PageImpl<>(List.of(testContract));
        when(contractRepository.findByContractingPartyNameContainingIgnoreCase(partyName, pageable))
                .thenReturn(expectedPage);

        // When
        Page<Contract> result = contractSearchService.searchByContractingPartyName(partyName, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(contractRepository).findByContractingPartyNameContainingIgnoreCase(partyName, pageable);
    }

    @Test
    void searchBySource_WithValidSource_ShouldReturnContracts() {
        // Given
        String source = "perfiles";
        Page<Contract> expectedPage = new PageImpl<>(List.of(testContract));
        when(contractRepository.findBySource(source, pageable)).thenReturn(expectedPage);

        // When
        Page<Contract> result = contractSearchService.searchBySource(source, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(contractRepository).findBySource(source, pageable);
    }

    @Test
    void searchByCountrySubentity_WithValidSubentity_ShouldReturnContracts() {
        // Given
        String countrySubentity = "Madrid";
        Page<Contract> expectedPage = new PageImpl<>(List.of(testContract));
        when(contractRepository.findByCountrySubentityContainingIgnoreCase(countrySubentity, pageable))
                .thenReturn(expectedPage);

        // When
        Page<Contract> result = contractSearchService.searchByCountrySubentity(countrySubentity, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(contractRepository).findByCountrySubentityContainingIgnoreCase(countrySubentity, pageable);
    }

    @Test
    void globalSearch_WithValidQuery_ShouldReturnContracts() {
        // Given
        String query = "infrastructure";
        Page<Contract> expectedPage = new PageImpl<>(List.of(testContract));
        when(contractRepository.findByGlobalSearch(query, pageable)).thenReturn(expectedPage);

        // When
        Page<Contract> result = contractSearchService.globalSearch(query, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(contractRepository).findByGlobalSearch(query, pageable);
    }

    @Test
    void getContractingPartyAutocomplete_WithValidQuery_ShouldReturnSuggestions() {
        // Given
        String query = "Test";
        List<String> expectedSuggestions = List.of("Test Organization 1", "Test Organization 2");
        when(contractRepository.findContractingPartyNamesByQuery(query)).thenReturn(expectedSuggestions);

        // When
        List<String> result = contractSearchService.getContractingPartyAutocomplete(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedSuggestions, result);
        verify(contractRepository).findContractingPartyNamesByQuery(query);
    }

    @Test
    void getContractingPartyAutocomplete_WithShortQuery_ShouldReturnEmpty() {
        // Given
        String query = "Te"; // Less than 3 characters

        // When
        List<String> result = contractSearchService.getContractingPartyAutocomplete(query);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(contractRepository);
    }

    @Test
    void getContractingPartyAutocomplete_WithNullQuery_ShouldReturnEmpty() {
        // Given
        String query = null;

        // When
        List<String> result = contractSearchService.getContractingPartyAutocomplete(query);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(contractRepository);
    }

    @Test
    void getGlobalAutocomplete_WithValidQuery_ShouldReturnSuggestions() {
        // Given
        String query = "infrastructure";
        List<Map<String, String>> expectedSuggestions = List.of(
                Map.of("value", "Infrastructure Project", "type", "title"),
                Map.of("value", "Infrastructure Department", "type", "organization")
        );
        when(contractRepository.findGlobalAutocomplete(query)).thenReturn(expectedSuggestions);

        // When
        List<Map<String, String>> result = contractSearchService.getGlobalAutocomplete(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedSuggestions, result);
        verify(contractRepository).findGlobalAutocomplete(query);
    }

    @Test
    void getGlobalAutocomplete_WithShortQuery_ShouldReturnEmpty() {
        // Given
        String query = "in"; // Less than 3 characters

        // When
        List<Map<String, String>> result = contractSearchService.getGlobalAutocomplete(query);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(contractRepository);
    }
}