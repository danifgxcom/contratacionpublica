package com.danifgx.contratacionpublica.repository;

import com.danifgx.contratacionpublica.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository for Contract entity.
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    /**
     * Find a contract by its external ID.
     *
     * @param externalId the external ID from the .atom file
     * @return the contract if found
     */
    Optional<Contract> findByExternalId(String externalId);

    /**
     * Check if a contract with the given external ID exists.
     *
     * @param externalId the external ID from the .atom file
     * @return true if the contract exists
     */
    boolean existsByExternalId(String externalId);

    /**
     * Find contracts by contracting party name (case-insensitive, partial match) with pagination.
     *
     * @param contractingPartyName the name of the contracting party
     * @param pageable the pagination information
     * @return a page of contracts
     */
    Page<Contract> findByContractingPartyNameContainingIgnoreCase(String contractingPartyName, Pageable pageable);

    /**
     * Find contracts by title (case-insensitive, partial match) with pagination.
     *
     * @param title the title of the contract
     * @param pageable the pagination information
     * @return a page of contracts
     */
    Page<Contract> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Find contracts by CPV code.
     *
     * @param cpvCode the CPV code
     * @return a list of contracts
     */
    List<Contract> findByCpvCode(String cpvCode);

    /**
     * Find contracts by NUTS code.
     *
     * @param nutsCode the NUTS code
     * @return a list of contracts
     */
    List<Contract> findByNutsCode(String nutsCode);

    /**
     * Count the number of contracts by type code.
     *
     * @return a list of counts by type code
     */
    @Query("SELECT COALESCE(c.typeCode, 'Unknown'), COUNT(c) FROM Contract c GROUP BY c.typeCode")
    List<Object[]> countByTypeCode();

    /**
     * Count the number of contracts by status.
     *
     * @return a list of counts by status
     */
    @Query("SELECT COALESCE(c.status, 'Unknown'), COUNT(c) FROM Contract c GROUP BY c.status")
    List<Object[]> countByStatus();

    /**
     * Find contracts by source file.
     *
     * @param sourceFile the source file name
     * @return a list of contracts
     */
    List<Contract> findBySourceFile(String sourceFile);

    /**
     * Find contracts by source with pagination.
     *
     * @param source the source of the contract data (perfiles or agregadas)
     * @param pageable the pagination information
     * @return a page of contracts
     */
    Page<Contract> findBySource(String source, Pageable pageable);

    /**
     * Count the number of contracts by source.
     *
     * @return a list of counts by source
     */
    @Query("SELECT COALESCE(c.source, 'Unknown'), COUNT(c) FROM Contract c GROUP BY c.source")
    List<Object[]> countBySource();

    /**
     * Get the top 10 contracting organizations by number of contracts and total amount.
     *
     * @return a list of top contracting organizations with their contract count and total amount
     */
    @Query("SELECT c.contractingPartyName, COUNT(c), " +
           "COALESCE(SUM(COALESCE(c.totalAmount, c.taxExclusiveAmount, c.estimatedAmount)), 0), " +
           "SUM(CASE WHEN COALESCE(c.totalAmount, c.taxExclusiveAmount, c.estimatedAmount) IS NOT NULL THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN COALESCE(c.totalAmount, c.taxExclusiveAmount, c.estimatedAmount) IS NULL THEN 1 ELSE 0 END) " +
           "FROM Contract c " +
           "WHERE c.contractingPartyName IS NOT NULL " +
           "GROUP BY c.contractingPartyName " +
           "ORDER BY COALESCE(SUM(COALESCE(c.totalAmount, c.taxExclusiveAmount, c.estimatedAmount)), 0) DESC " +
           "LIMIT 10")
    List<Object[]> findTopContractingOrganizations();

    /**
     * Get distinct years from contract updated dates.
     *
     * @return a list of distinct years
     */
    @Query("SELECT DISTINCT EXTRACT(YEAR FROM c.updatedAt) FROM Contract c WHERE c.updatedAt IS NOT NULL ORDER BY EXTRACT(YEAR FROM c.updatedAt) DESC")
    List<Integer> findDistinctYears();

    /**
     * Get distinct regions (NUTS codes and names).
     *
     * @return a list of region codes and names
     */
    @Query("SELECT DISTINCT c.nutsCode, c.countrySubentity FROM Contract c WHERE c.nutsCode IS NOT NULL AND c.countrySubentity IS NOT NULL ORDER BY c.countrySubentity")
    List<Object[]> findDistinctRegions();

    /**
     * Get autocomplete suggestions for contracting party names.
     *
     * @param query the search query
     * @return a list of matching contracting party names
     */
    @Query("SELECT DISTINCT c.contractingPartyName FROM Contract c WHERE LOWER(c.contractingPartyName) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY c.contractingPartyName LIMIT 50")
    List<String> findContractingPartyNamesByQuery(@Param("query") String query);

    /**
     * Count the number of contracts by autonomous community (country subentity).
     *
     * @return a list of counts by autonomous community
     */
    @Query("SELECT COALESCE(c.countrySubentity, 'Unknown'), COUNT(c), " +
           "COALESCE(SUM(COALESCE(c.totalAmount, c.taxExclusiveAmount, c.estimatedAmount)), 0), " +
           "ROUND(AVG(COALESCE(c.totalAmount, c.taxExclusiveAmount, c.estimatedAmount)), 2) " +
           "FROM Contract c " +
           "GROUP BY c.countrySubentity " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> countByAutonomousCommunity();
}
