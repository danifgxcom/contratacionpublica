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
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalContracts", totalContracts);
        stats.put("countByTypeCode", getStatisticsByTypeCode());
        stats.put("countByStatus", getStatisticsByStatus());
        stats.put("countBySource", getStatisticsBySource());
        stats.put("topOrganizations", getTopContractingOrganizations());
        stats.put("countByAutonomousCommunity", getStatisticsByAutonomousCommunity());
        
        // Enhanced analytics
        stats.put("amountAnalysis", getAmountAnalysis());
        stats.put("monthlyTrends", getMonthlyTrends());
        stats.put("contractValueDistribution", getContractValueDistribution());
        stats.put("topRegionsByValue", getTopRegionsByValue());
        stats.put("typeVsAmountAnalysis", getTypeVsAmountAnalysis());
        stats.put("sourceEfficiency", getSourceEfficiencyAnalysis());
        stats.put("recentActivity", getRecentActivityStats());
        
        return stats;
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

    /**
     * Get comprehensive amount analysis.
     */
    private Map<String, Object> getAmountAnalysis() {
        List<Object[]> amountStats = contractRepository.getAmountStatistics();
        Map<String, Object> analysis = new HashMap<>();
        
        if (!amountStats.isEmpty()) {
            Object[] stats = amountStats.get(0);
            analysis.put("totalAmount", stats[0]);
            analysis.put("averageAmount", stats[1]);
            analysis.put("maxAmount", stats[2]);
            analysis.put("minAmount", stats[3]);
            analysis.put("contractsWithAmount", stats[4]);
            analysis.put("contractsWithoutAmount", stats[5]);
            analysis.put("amountCoverage", stats[6]); // percentage of contracts with amount
        }
        
        // Add max amount contract details for linking
        List<Object[]> maxContract = contractRepository.getMaxAmountContract();
        if (!maxContract.isEmpty()) {
            Object[] contractData = maxContract.get(0);
            Map<String, Object> maxContractInfo = new HashMap<>();
            maxContractInfo.put("id", contractData[0]);
            maxContractInfo.put("title", contractData[1]);
            maxContractInfo.put("contractingPartyName", contractData[2]);
            maxContractInfo.put("amount", contractData[3]);
            analysis.put("maxAmountContract", maxContractInfo);
        }
        
        return analysis;
    }

    /**
     * Get monthly contract trends.
     */
    private List<Map<String, Object>> getMonthlyTrends() {
        return contractRepository.getMonthlyTrends().stream()
                .map(arr -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("year", arr[0]);
                    result.put("month", arr[1]);
                    result.put("contractCount", arr[2]);
                    result.put("totalAmount", arr[3]);
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get contract value distribution analysis.
     */
    private Map<String, Object> getContractValueDistribution() {
        List<Object[]> distribution = contractRepository.getContractValueDistribution();
        Map<String, Object> result = new HashMap<>();
        
        result.put("microContracts", 0L);    // < 40,000 EUR
        result.put("smallContracts", 0L);    // 40,000 - 144,000 EUR  
        result.put("mediumContracts", 0L);   // 144,000 - 750,000 EUR
        result.put("largeContracts", 0L);    // > 750,000 EUR
        
        for (Object[] row : distribution) {
            String range = (String) row[0];
            Long count = (Long) row[1];
            result.put(range, count);
        }
        
        return result;
    }

    /**
     * Get top regions by total contract value.
     */
    private List<Map<String, Object>> getTopRegionsByValue() {
        return contractRepository.getTopRegionsByValue().stream()
                .limit(10)
                .map(arr -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("region", arr[0]);
                    result.put("totalAmount", arr[1]);
                    result.put("contractCount", arr[2]);
                    result.put("averageAmount", arr[3]);
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get contract type vs amount analysis.
     */
    private List<Map<String, Object>> getTypeVsAmountAnalysis() {
        return contractRepository.getTypeAmountAnalysis().stream()
                .map(arr -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("typeCode", arr[0]);
                    result.put("description", contractTypeService.getDescriptionForCode((String) arr[0]));
                    result.put("contractCount", arr[1]);
                    result.put("totalAmount", arr[2]);
                    result.put("averageAmount", arr[3]);
                    result.put("maxAmount", arr[4]);
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get source efficiency analysis (avg amount per source).
     */
    private List<Map<String, Object>> getSourceEfficiencyAnalysis() {
        return contractRepository.getSourceEfficiencyStats().stream()
                .map(arr -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("source", arr[0]);
                    result.put("contractCount", arr[1]);
                    result.put("totalAmount", arr[2]);
                    result.put("averageAmount", arr[3]);
                    result.put("amountCoverage", arr[4]); // percentage with amount data
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get recent activity statistics (last 30 days).
     */
    private Map<String, Object> getRecentActivityStats() {
        List<Object[]> recentStats = contractRepository.getRecentActivityStats();
        Map<String, Object> result = new HashMap<>();
        
        if (!recentStats.isEmpty()) {
            Object[] stats = recentStats.get(0);
            result.put("contractsLast30Days", stats[0]);
            result.put("contractsLast7Days", stats[1]);
            result.put("amountLast30Days", stats[2]);
            result.put("amountLast7Days", stats[3]);
            result.put("avgDailyContracts", stats[4]);
        }
        
        return result;
    }
}