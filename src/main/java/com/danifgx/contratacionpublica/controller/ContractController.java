package com.danifgx.contratacionpublica.controller;

import com.danifgx.contratacionpublica.model.Contract;
import com.danifgx.contratacionpublica.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for contract operations.
 */
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContractController {

    private final ContractService contractService;

    /**
     * Get a contract by its ID.
     *
     * @param id the ID of the contract
     * @return the contract if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContractById(@PathVariable UUID id) {
        return contractService.getContractById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all contracts with pagination.
     *
     * @param pageable the pagination information
     * @return a page of contracts
     */
    @GetMapping
    public ResponseEntity<Page<Contract>> getAllContracts(Pageable pageable) {
        return ResponseEntity.ok(contractService.getAllContracts(pageable));
    }

    /**
     * Search contracts by title.
     *
     * @param title the title to search for
     * @param pageable the pagination information
     * @return a page of contracts
     */
    @GetMapping("/search/title")
    public ResponseEntity<Page<Contract>> searchContractsByTitle(@RequestParam String title, Pageable pageable) {
        return ResponseEntity.ok(contractService.searchContractsByTitle(title, pageable));
    }

    /**
     * Search contracts by contracting party name.
     *
     * @param name the contracting party name to search for
     * @param pageable the pagination information
     * @return a page of contracts
     */
    @GetMapping("/search/contracting-party")
    public ResponseEntity<Page<Contract>> searchContractsByContractingPartyName(@RequestParam String name, Pageable pageable) {
        return ResponseEntity.ok(contractService.searchContractsByContractingPartyName(name, pageable));
    }

    /**
     * Find contracts by source.
     *
     * @param source the source of the contract data (perfiles or agregadas)
     * @param pageable the pagination information
     * @return a page of contracts
     */
    @GetMapping("/search/source")
    public ResponseEntity<Page<Contract>> findContractsBySource(@RequestParam String source, Pageable pageable) {
        return ResponseEntity.ok(contractService.findContractsBySource(source, pageable));
    }

    /**
     * Get autocomplete suggestions for contracting party names.
     *
     * @param query the search query (minimum 3 characters)
     * @return a list of matching contracting party names
     */
    @GetMapping("/autocomplete/contracting-parties")
    public ResponseEntity<List<String>> getContractingPartyAutocomplete(@RequestParam String query) {
        if (query.length() < 3) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(contractService.getContractingPartyAutocomplete(query));
    }

    /**
     * Get global search autocomplete suggestions.
     *
     * @param query the search query (minimum 3 characters)
     * @return a list of matching suggestions from multiple fields
     */
    @GetMapping("/autocomplete/global")
    public ResponseEntity<List<Map<String, String>>> getGlobalAutocomplete(@RequestParam String query) {
        if (query.length() < 3) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(contractService.getGlobalAutocomplete(query));
    }

    /**
     * Global search in multiple fields.
     *
     * @param query the search query
     * @param pageable pagination parameters
     * @return a page of contracts matching the query
     */
    @GetMapping("/search/global")
    public ResponseEntity<Page<Contract>> globalSearch(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(contractService.globalSearch(query, pageable));
    }

    /**
     * Get statistics about contracts.
     *
     * @return a map of statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getContractStatistics() {
        return ResponseEntity.ok(contractService.getContractStatistics());
    }

    /**
     * Get the count of contracts in the database.
     *
     * @return the count of contracts
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getContractCount() {
        return ResponseEntity.ok(contractService.getContractCount());
    }

    /**
     * Get distinct years from contract updated dates.
     *
     * @return a list of distinct years
     */
    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getDistinctYears() {
        return ResponseEntity.ok(contractService.getDistinctYears());
    }

    /**
     * Get distinct regions (NUTS codes and names).
     *
     * @return a map of region codes to names
     */
    @GetMapping("/regions")
    public ResponseEntity<Map<String, String>> getDistinctRegions() {
        return ResponseEntity.ok(contractService.getDistinctRegions());
    }

    /**
     * Get statistics by autonomous community.
     *
     * @return a list of statistics by autonomous community
     */
    @GetMapping("/statistics/autonomous-communities")
    public ResponseEntity<List<Map<String, Object>>> getStatisticsByAutonomousCommunity() {
        return ResponseEntity.ok(contractService.getStatisticsByAutonomousCommunity());
    }
}
