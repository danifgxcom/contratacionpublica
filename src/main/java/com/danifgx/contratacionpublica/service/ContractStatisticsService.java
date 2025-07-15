package com.danifgx.contratacionpublica.service;

import com.danifgx.contratacionpublica.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for contract statistics operations.
 * Follows Single Responsibility Principle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContractStatisticsService {

    private final ContractRepository contractRepository;
    private final ContractTypeService contractTypeService;

    /**
     * Get comprehensive contract statistics.
     *
     * @return a map containing various statistics
     */
    public Map<String, Object> getComprehensiveStatistics() {
        log.debug("Generating comprehensive contract statistics");
        
        long totalContracts = contractRepository.count();
        
        return Map.of(
            "totalContracts", totalContracts,
            "countByTypeCode", getStatisticsByTypeCode(),
            "countByStatus", getStatisticsByStatus(),
            "countBySource", getStatisticsBySource(),
            "topOrganizations", getTopContractingOrganizations(),
            "countByAutonomousCommunity", getStatisticsByAutonomousCommunity()
        );
    }

    /**
     * Get statistics by autonomous community.
     *
     * @return a list of statistics by autonomous community
     */
    public List<Map<String, Object>> getStatisticsByAutonomousCommunity() {
        log.debug("Getting statistics by autonomous community");
        return contractRepository.countByAutonomousCommunity().stream()
                .map(this::mapAutonomousCommunityStatistic)
                .collect(Collectors.toList());
    }

    /**
     * Get distinct years from contract updated dates.
     *
     * @return a list of distinct years
     */
    public List<Integer> getDistinctYears() {
        log.debug("Getting distinct years");
        return contractRepository.findDistinctYears();
    }

    /**
     * Get distinct regions (NUTS codes and names).
     *
     * @return a map of region codes to names
     */
    public Map<String, String> getDistinctRegions() {
        log.debug("Getting distinct regions");
        return contractRepository.findDistinctRegions().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],  // NUTS code
                        arr -> (String) arr[1],  // Region name
                        (existing, replacement) -> existing  // Keep existing on duplicate
                ));
    }

    private Map<String, Object> getStatisticsByTypeCode() {
        return contractRepository.countByTypeCode().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> {
                            String code = (String) arr[0];
                            Long count = (Long) arr[1];
                            String description = contractTypeService.getDescriptionForCode(code);
                            Map<String, Object> result = new HashMap<>();
                            result.put("count", count);
                            result.put("description", description);
                            return result;
                        }
                ));
    }

    private Map<String, Long> getStatisticsByStatus() {
        return contractRepository.countByStatus().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    private Map<String, Long> getStatisticsBySource() {
        return contractRepository.countBySource().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    private List<Map<String, Object>> getTopContractingOrganizations() {
        return contractRepository.findTopContractingOrganizations().stream()
                .map(arr -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("name", arr[0]);
                    result.put("contractCount", arr[1]);
                    result.put("totalAmount", arr[2]);
                    result.put("contractsWithAmount", arr[3]);
                    result.put("contractsWithoutAmount", arr[4]);
                    return result;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapAutonomousCommunityStatistic(Object[] arr) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", arr[0]);
        result.put("contractCount", arr[1]);
        result.put("totalAmount", arr[2]);
        result.put("averageAmount", arr[3]);
        return result;
    }
}