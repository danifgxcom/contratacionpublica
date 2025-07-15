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

import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for contract query operations.
 * Follows Single Responsibility Principle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContractQueryService {

    private final ContractRepository contractRepository;

    /**
     * Find a contract by its ID.
     *
     * @param id the contract ID
     * @return the contract if found
     */
    public Optional<Contract> findById(UUID id) {
        log.debug("Finding contract by ID: {}", id);
        return contractRepository.findById(id);
    }

    /**
     * Find a contract by its external ID.
     *
     * @param externalId the external ID from the .atom file
     * @return the contract if found
     */
    public Optional<Contract> findByExternalId(String externalId) {
        log.debug("Finding contract by external ID: {}", externalId);
        return contractRepository.findByExternalId(externalId);
    }

    /**
     * Get all contracts with pagination.
     *
     * @param pageable the pagination information
     * @return a page of contracts
     */
    public Page<Contract> findAll(Pageable pageable) {
        log.info("🔥 CONTRACT QUERY SERVICE - Finding all contracts with pagination: {}", pageable);
        
        // Check if sorting by amount fields and use custom queries
        if (pageable.getSort().isSorted()) {
            log.info("🎯 SORT IS DETECTED, checking fields...");
            for (Sort.Order order : pageable.getSort()) {
                log.info("🔍 Checking sort field: {}, direction: {}", order.getProperty(), order.getDirection());
                if (isAmountField(order.getProperty())) {
                    log.info("🎯 USING CUSTOM AMOUNT SORTING for field: {} with direction: {}", order.getProperty(), order.getDirection());
                    // Create pageable without sort for the custom query (it has its own ORDER BY)
                    Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
                    
                    if (order.getDirection() == Sort.Direction.ASC) {
                        log.info("🔼 Executing ASCENDING custom query");
                        return contractRepository.findAllOrderByEffectiveAmountAsc(unsortedPageable);
                    } else {
                        log.info("🔽 Executing DESCENDING custom query");
                        return contractRepository.findAllOrderByEffectiveAmountDesc(unsortedPageable);
                    }
                } else {
                    log.debug("Field {} is not an amount field, using standard sorting", order.getProperty());
                }
            }
        } else {
            log.debug("No sorting detected, using standard findAll");
        }
        
        return contractRepository.findAll(pageable);
    }

    /**
     * Get the total count of contracts.
     *
     * @return the count of contracts
     */
    public long count() {
        log.debug("Counting all contracts");
        return contractRepository.count();
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