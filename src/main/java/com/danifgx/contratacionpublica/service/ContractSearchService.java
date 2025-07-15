package com.danifgx.contratacionpublica.service;

import com.danifgx.contratacionpublica.model.Contract;
import com.danifgx.contratacionpublica.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Service responsible for contract search operations.
 * Follows Single Responsibility Principle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContractSearchService {

    private final ContractRepository contractRepository;

    /**
     * Search contracts by title.
     *
     * @param title the title to search for
     * @param pageable the pagination information
     * @return a page of matching contracts
     */
    public Page<Contract> searchByTitle(String title, Pageable pageable) {
        validateSearchTerm(title, "title");
        log.debug("Searching contracts by title: {}", title);
        Pageable adjustedPageable = adjustPageableForAmountSorting(pageable);
        return contractRepository.findByTitleContainingIgnoreCase(title, adjustedPageable);
    }

    /**
     * Search contracts by contracting party name.
     *
     * @param contractingPartyName the contracting party name to search for
     * @param pageable the pagination information
     * @return a page of matching contracts
     */
    public Page<Contract> searchByContractingPartyName(String contractingPartyName, Pageable pageable) {
        validateSearchTerm(contractingPartyName, "contracting party name");
        log.debug("Searching contracts by contracting party: {}", contractingPartyName);
        Pageable adjustedPageable = adjustPageableForAmountSorting(pageable);
        return contractRepository.findByContractingPartyNameContainingIgnoreCase(contractingPartyName, adjustedPageable);
    }

    /**
     * Search contracts by source.
     *
     * @param source the source to search for
     * @param pageable the pagination information
     * @return a page of matching contracts
     */
    public Page<Contract> searchBySource(String source, Pageable pageable) {
        validateSearchTerm(source, "source");
        log.debug("Searching contracts by source: {}", source);
        Pageable adjustedPageable = adjustPageableForAmountSorting(pageable);
        return contractRepository.findBySource(source, adjustedPageable);
    }

    /**
     * Search contracts by country subentity (autonomous community).
     *
     * @param countrySubentity the autonomous community name
     * @param pageable the pagination information
     * @return a page of matching contracts
     */
    public Page<Contract> searchByCountrySubentity(String countrySubentity, Pageable pageable) {
        validateSearchTerm(countrySubentity, "country subentity");
        log.debug("Searching contracts by country subentity: {}", countrySubentity);
        Pageable adjustedPageable = adjustPageableForAmountSorting(pageable);
        return contractRepository.findByCountrySubentityContainingIgnoreCase(countrySubentity, adjustedPageable);
    }

    /**
     * Global search across multiple fields.
     *
     * @param query the search query
     * @param pageable the pagination information
     * @return a page of matching contracts
     */
    public Page<Contract> globalSearch(String query, Pageable pageable) {
        validateSearchTerm(query, "global search query");
        log.debug("Performing global search: {}", query);
        Pageable adjustedPageable = adjustPageableForAmountSorting(pageable);
        return contractRepository.findByGlobalSearch(query, adjustedPageable);
    }

    /**
     * Get autocomplete suggestions for contracting party names.
     *
     * @param query the search query (minimum 3 characters)
     * @return a list of matching contracting party names
     */
    public List<String> getContractingPartyAutocomplete(String query) {
        if (!StringUtils.hasText(query) || query.length() < 3) {
            log.debug("Query too short for autocomplete: {}", query);
            return List.of();
        }
        
        log.debug("Getting contracting party autocomplete for: {}", query);
        return contractRepository.findContractingPartyNamesByQuery(query);
    }

    /**
     * Get global search autocomplete suggestions.
     *
     * @param query the search query (minimum 3 characters)
     * @return a list of matching suggestions with their types
     */
    public List<Map<String, String>> getGlobalAutocomplete(String query) {
        if (!StringUtils.hasText(query) || query.length() < 3) {
            log.debug("Query too short for global autocomplete: {}", query);
            return List.of();
        }
        
        log.debug("Getting global autocomplete for: {}", query);
        return contractRepository.findGlobalAutocomplete(query);
    }

    private void validateSearchTerm(String searchTerm, String fieldName) {
        if (!StringUtils.hasText(searchTerm)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    
    /**
     * Adjusts pageable to handle amount field sorting properly.
     * Since Spring Data JPA doesn't support nullsLast() with Criteria Queries,
     * we just pass through the sorting. The database will handle NULLs according 
     * to its default behavior (PostgreSQL puts NULLs last by default for ASC).
     */
    private Pageable adjustPageableForAmountSorting(Pageable pageable) {
        return pageable;
    }
    
    /**
     * Checks if the given property is an amount field that needs special null handling.
     */
    private boolean isAmountField(String property) {
        return property.equals("estimatedAmount") || 
               property.equals("totalAmount") || 
               property.equals("taxExclusiveAmount");
    }
}