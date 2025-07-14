package com.danifgx.contratacionpublica.service;

import com.danifgx.contratacionpublica.model.Contract;
import com.danifgx.contratacionpublica.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling contract operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractTypeService contractTypeService;

    /**
     * Get a contract by its ID.
     *
     * @param id the ID of the contract
     * @return the contract if found
     */
    @Transactional(readOnly = true)
    public Optional<Contract> getContractById(UUID id) {
        return contractRepository.findById(id);
    }

    /**
     * Get a contract by its external ID.
     *
     * @param externalId the external ID of the contract
     * @return the contract if found
     */
    @Transactional(readOnly = true)
    public Optional<Contract> getContractByExternalId(String externalId) {
        return contractRepository.findByExternalId(externalId);
    }

    /**
     * Get all contracts with pagination.
     *
     * @param pageable the pagination information
     * @return a page of contracts
     */
    @Transactional(readOnly = true)
    public Page<Contract> getAllContracts(Pageable pageable) {
        return contractRepository.findAll(pageable);
    }

    /**
     * Search contracts by title with pagination.
     *
     * @param title the title to search for
     * @param pageable the pagination information
     * @return a page of contracts
     */
    @Transactional(readOnly = true)
    public Page<Contract> searchContractsByTitle(String title, Pageable pageable) {
        return contractRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    /**
     * Search contracts by contracting party name with pagination.
     *
     * @param contractingPartyName the contracting party name to search for
     * @param pageable the pagination information
     * @return a page of contracts
     */
    @Transactional(readOnly = true)
    public Page<Contract> searchContractsByContractingPartyName(String contractingPartyName, Pageable pageable) {
        return contractRepository.findByContractingPartyNameContainingIgnoreCase(contractingPartyName, pageable);
    }

    /**
     * Find contracts by source with pagination.
     *
     * @param source the source of the contract data (perfiles or agregadas)
     * @param pageable the pagination information
     * @return a page of contracts
     */
    @Transactional(readOnly = true)
    public Page<Contract> findContractsBySource(String source, Pageable pageable) {
        return contractRepository.findBySource(source, pageable);
    }

    /**
     * Get statistics about contracts.
     *
     * @return a map of statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getContractStatistics() {
        long totalContracts = contractRepository.count();

        // Count by type code with descriptions
        Map<String, Object> countByTypeCode = contractRepository.countByTypeCode().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> {
                            String code = (String) arr[0];
                            Long count = (Long) arr[1];
                            String description = contractTypeService.getDescriptionForCode(code);
                            return Map.of(
                                    "count", count,
                                    "description", description
                            );
                        }
                ));

        // Count by status
        Map<String, Long> countByStatus = contractRepository.countByStatus().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));

        // Count by source
        Map<String, Long> countBySource = contractRepository.countBySource().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));

        // Top contracting organizations
        List<Map<String, Object>> topOrganizations = contractRepository.findTopContractingOrganizations().stream()
                .map(arr -> {
                    Map<String, Object> orgMap = new java.util.HashMap<>();
                    orgMap.put("name", arr[0]);
                    orgMap.put("contractCount", arr[1]);
                    orgMap.put("totalAmount", arr[2]);
                    orgMap.put("contractsWithAmount", arr[3]);
                    orgMap.put("contractsWithoutAmount", arr[4]);
                    return orgMap;
                })
                .collect(Collectors.toList());

        // Count by autonomous community
        List<Map<String, Object>> countByAutonomousCommunity = contractRepository.countByAutonomousCommunity().stream()
                .map(arr -> {
                    Map<String, Object> communityMap = new java.util.HashMap<>();
                    communityMap.put("name", arr[0]);
                    communityMap.put("contractCount", arr[1]);
                    communityMap.put("totalAmount", arr[2]);
                    communityMap.put("averageAmount", arr[3]);
                    return communityMap;
                })
                .collect(Collectors.toList());

        return Map.of(
                "totalContracts", totalContracts,
                "countByTypeCode", countByTypeCode,
                "countByStatus", countByStatus,
                "countBySource", countBySource,
                "topOrganizations", topOrganizations,
                "countByAutonomousCommunity", countByAutonomousCommunity
        );
    }

    /**
     * Get the count of contracts in the database.
     *
     * @return the count of contracts
     */
    @Transactional(readOnly = true)
    public Long getContractCount() {
        return contractRepository.count();
    }

    /**
     * Get distinct years from contract updated dates.
     *
     * @return a list of distinct years
     */
    @Transactional(readOnly = true)
    public List<Integer> getDistinctYears() {
        return contractRepository.findDistinctYears();
    }

    /**
     * Get distinct regions (NUTS codes and names).
     *
     * @return a map of region codes to names
     */
    @Transactional(readOnly = true)
    public Map<String, String> getDistinctRegions() {
        return contractRepository.findDistinctRegions().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],  // NUTS code
                        arr -> (String) arr[1],  // Region name
                        (existing, replacement) -> existing  // In case of duplicate keys, keep the existing one
                ));
    }

    /**
     * Get statistics by autonomous community.
     *
     * @return a list of statistics by autonomous community
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStatisticsByAutonomousCommunity() {
        return contractRepository.countByAutonomousCommunity().stream()
                .map(arr -> {
                    Map<String, Object> communityMap = new java.util.HashMap<>();
                    communityMap.put("name", arr[0]);
                    communityMap.put("contractCount", arr[1]);
                    communityMap.put("totalAmount", arr[2]);
                    communityMap.put("averageAmount", arr[3]);
                    return communityMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get autocomplete suggestions for contracting party names.
     *
     * @param query the search query
     * @return a list of matching contracting party names
     */
    @Transactional(readOnly = true)
    public List<String> getContractingPartyAutocomplete(String query) {
        List<String> results = contractRepository.findContractingPartyNamesByQuery(query);
        
        // Sort to prioritize names that start with the query
        String lowerQuery = query.toLowerCase();
        return results.stream()
                .sorted((a, b) -> {
                    boolean aStartsWith = a.toLowerCase().startsWith(lowerQuery);
                    boolean bStartsWith = b.toLowerCase().startsWith(lowerQuery);
                    
                    if (aStartsWith && !bStartsWith) return -1;
                    if (!aStartsWith && bStartsWith) return 1;
                    return a.compareToIgnoreCase(b);
                })
                .collect(Collectors.toList());
    }
}
