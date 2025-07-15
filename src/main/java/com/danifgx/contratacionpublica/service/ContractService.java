package com.danifgx.contratacionpublica.service;

import com.danifgx.contratacionpublica.model.Contract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Facade service for contract operations.
 * Delegates to specialized services following the Facade pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractQueryService contractQueryService;
    private final ContractSearchService contractSearchService;
    private final ContractStatisticsService contractStatisticsService;

    // Query operations - delegate to ContractQueryService
    public Optional<Contract> getContractById(UUID id) {
        return contractQueryService.findById(id);
    }

    public Optional<Contract> getContractByExternalId(String externalId) {
        return contractQueryService.findByExternalId(externalId);
    }

    public Page<Contract> getAllContracts(Pageable pageable) {
        return contractQueryService.findAll(pageable);
    }

    public Long getContractCount() {
        return contractQueryService.count();
    }

    // Search operations - delegate to ContractSearchService
    public Page<Contract> searchContractsByTitle(String title, Pageable pageable) {
        return contractSearchService.searchByTitle(title, pageable);
    }

    public Page<Contract> searchContractsByContractingPartyName(String contractingPartyName, Pageable pageable) {
        return contractSearchService.searchByContractingPartyName(contractingPartyName, pageable);
    }

    public Page<Contract> findContractsBySource(String source, Pageable pageable) {
        return contractSearchService.searchBySource(source, pageable);
    }

    public Page<Contract> findContractsByCountrySubentity(String countrySubentity, Pageable pageable) {
        return contractSearchService.searchByCountrySubentity(countrySubentity, pageable);
    }

    public Page<Contract> globalSearch(String query, Pageable pageable) {
        return contractSearchService.globalSearch(query, pageable);
    }

    public List<String> getContractingPartyAutocomplete(String query) {
        return contractSearchService.getContractingPartyAutocomplete(query);
    }

    public List<Map<String, String>> getGlobalAutocomplete(String query) {
        return contractSearchService.getGlobalAutocomplete(query);
    }

    // Statistics operations - delegate to ContractStatisticsService
    public Map<String, Object> getContractStatistics() {
        return contractStatisticsService.getComprehensiveStatistics();
    }

    public List<Map<String, Object>> getStatisticsByAutonomousCommunity() {
        return contractStatisticsService.getStatisticsByAutonomousCommunity();
    }

    public List<Integer> getDistinctYears() {
        return contractStatisticsService.getDistinctYears();
    }

    public Map<String, String> getDistinctRegions() {
        return contractStatisticsService.getDistinctRegions();
    }
}
