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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractQueryServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private ContractQueryService contractQueryService;

    private Contract testContract;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testContract = new Contract();
        testContract.setId(testId);
        testContract.setExternalId("EXT-123");
        testContract.setTitle("Test Contract");
    }

    @Test
    void findById_WhenContractExists_ShouldReturnContract() {
        // Given
        when(contractRepository.findById(testId)).thenReturn(Optional.of(testContract));

        // When
        Optional<Contract> result = contractQueryService.findById(testId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testContract, result.get());
        verify(contractRepository).findById(testId);
    }

    @Test
    void findById_WhenContractNotExists_ShouldReturnEmpty() {
        // Given
        when(contractRepository.findById(testId)).thenReturn(Optional.empty());

        // When
        Optional<Contract> result = contractQueryService.findById(testId);

        // Then
        assertFalse(result.isPresent());
        verify(contractRepository).findById(testId);
    }

    @Test
    void findByExternalId_WhenContractExists_ShouldReturnContract() {
        // Given
        String externalId = "EXT-123";
        when(contractRepository.findByExternalId(externalId)).thenReturn(Optional.of(testContract));

        // When
        Optional<Contract> result = contractQueryService.findByExternalId(externalId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testContract, result.get());
        verify(contractRepository).findByExternalId(externalId);
    }

    @Test
    void findByExternalId_WhenContractNotExists_ShouldReturnEmpty() {
        // Given
        String externalId = "NONEXISTENT";
        when(contractRepository.findByExternalId(externalId)).thenReturn(Optional.empty());

        // When
        Optional<Contract> result = contractQueryService.findByExternalId(externalId);

        // Then
        assertFalse(result.isPresent());
        verify(contractRepository).findByExternalId(externalId);
    }

    @Test
    void findAll_ShouldReturnPageOfContracts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Contract> contracts = List.of(testContract);
        Page<Contract> expectedPage = new PageImpl<>(contracts, pageable, 1);
        when(contractRepository.findAll(pageable)).thenReturn(expectedPage);

        // When
        Page<Contract> result = contractQueryService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testContract, result.getContent().get(0));
        verify(contractRepository).findAll(pageable);
    }

    @Test
    void count_ShouldReturnTotalCount() {
        // Given
        long expectedCount = 42L;
        when(contractRepository.count()).thenReturn(expectedCount);

        // When
        long result = contractQueryService.count();

        // Then
        assertEquals(expectedCount, result);
        verify(contractRepository).count();
    }
}