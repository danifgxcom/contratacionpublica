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
import org.springframework.data.jpa.repository.Query;

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
     * Find contracts by country subentity (autonomous community) with pagination.
     *
     * @param countrySubentity the autonomous community name
     * @param pageable the pagination information
     * @return a page of contracts
     */
    Page<Contract> findByCountrySubentityContainingIgnoreCase(String countrySubentity, Pageable pageable);

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
    @Query("SELECT " +
           "CASE " +
           "   WHEN c.countrySubentity LIKE '%Madrid%' OR c.countrySubentity = 'COMUNIDAD DE MADRID' THEN 'Comunidad de Madrid' " +
           "   WHEN c.countrySubentity IN ('Barcelona', 'Girona', 'Lleida', 'Tarragona', 'Cataluña') THEN 'Catalunya' " +
           "   WHEN c.countrySubentity IN ('Valencia/València', 'Alicante/Alacant', 'Alicante (Alacant)', 'Alicante / Alacant', 'Valencia / València', 'Valencia', 'València/Valencia', 'Castellón/Castelló', 'Comunidad Valenciana', 'Comunitat Valenciana') THEN 'Comunitat Valenciana' " +
           "   WHEN c.countrySubentity IN ('Sevilla', 'Málaga', 'Cádiz', 'Córdoba', 'Almería', 'Granada', 'Jaén', 'Huelva', 'Andalucía') THEN 'Andalucía' " +
           "   WHEN c.countrySubentity IN ('Zaragoza', 'Huesca', 'Teruel', 'Aragón') THEN 'Aragón' " +
           "   WHEN c.countrySubentity IN ('Asturias', 'Principado de Asturias') THEN 'Principado de Asturias' " +
           "   WHEN c.countrySubentity IN ('Cantabria') THEN 'Cantabria' " +
           "   WHEN c.countrySubentity IN ('Burgos', 'León', 'Palencia', 'Salamanca', 'Segovia', 'Soria', 'Valladolid', 'Zamora', 'Ávila', 'Castilla y León') THEN 'Castilla y León' " +
           "   WHEN c.countrySubentity IN ('Albacete', 'Ciudad Real', 'Cuenca', 'Guadalajara', 'Toledo', 'Castilla-La Mancha') THEN 'Castilla-La Mancha' " +
           "   WHEN c.countrySubentity IN ('Badajoz', 'Cáceres', 'Extremadura') THEN 'Extremadura' " +
           "   WHEN c.countrySubentity IN ('A Coruña', 'Lugo', 'Ourense', 'Ourense (Orense)', 'Pontevedra', 'Coruña, A', 'Galicia') THEN 'Galicia' " +
           "   WHEN c.countrySubentity IN ('Illes Balears', 'Mallorca', 'Menorca', 'Eivissa y Formentera', 'Palma de Mallorca') THEN 'Illes Balears' " +
           "   WHEN c.countrySubentity IN ('Canarias', 'CANARIAS', 'Las Palmas', 'Santa Cruz de Tenerife', 'Gran Canaria', 'Tenerife', 'Lanzarote', 'Fuerteventura', 'La Palma', 'La Gomera', 'El Hierro') THEN 'Canarias' " +
           "   WHEN c.countrySubentity IN ('La Rioja', 'Rioja, La') THEN 'La Rioja' " +
           "   WHEN c.countrySubentity IN ('Murcia', 'Región de Murcia') THEN 'Región de Murcia' " +
           "   WHEN c.countrySubentity IN ('Navarra', 'Comunidad Foral de Navarra') THEN 'Comunidad Foral de Navarra' " +
           "   WHEN c.countrySubentity IN ('Álava', 'Araba/Álava', 'Araba / Álava', 'Bizkaia', 'Vizcaya', 'Gipuzkoa', 'Guipúzcoa', 'País Vasco') THEN 'País Vasco' " +
           "   WHEN c.countrySubentity IN ('Ceuta', 'Ciudad de Ceuta') THEN 'Ceuta' " +
           "   WHEN c.countrySubentity IN ('Melilla', 'Ciudad de Melilla') THEN 'Melilla' " +
           "   WHEN c.countrySubentity IN ('Unknown', 'España', 'ESPAÑA', 'ES', 'Extra-Regio NUTS 1') THEN 'Sin clasificar' " +
           "   ELSE 'Exterior' " +
           "END as autonomousCommunity, " +
           "COUNT(c) as contractCount, " +
           "COALESCE(SUM(COALESCE(c.totalAmount, c.taxExclusiveAmount, c.estimatedAmount)), 0) as totalAmount, " +
           "COALESCE(ROUND(AVG(COALESCE(c.totalAmount, c.taxExclusiveAmount, c.estimatedAmount)), 2), 0) as averageAmount " +
           "FROM Contract c " +
           "GROUP BY autonomousCommunity " +
           "ORDER BY contractCount DESC")
    List<Object[]> countByAutonomousCommunity();

    /**
     * Global search autocomplete suggestions from multiple fields.
     *
     * @param query the search query
     * @return a list of suggestions with their types
     */
    @Query("SELECT DISTINCT c.title as text, 'title' as type FROM Contract c " +
           "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "UNION " +
           "SELECT DISTINCT c.contractingPartyName as text, 'contracting_party' as type FROM Contract c " +
           "WHERE LOWER(c.contractingPartyName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY text LIMIT 30")
    List<Map<String, String>> findGlobalAutocomplete(@Param("query") String query);

    /**
     * Global search in multiple fields (title, contracting party name).
     *
     * @param query the search query
     * @param pageable pagination parameters
     * @return a page of contracts matching the query
     */
    @Query("SELECT c FROM Contract c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.contractingPartyName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Contract> findByGlobalSearch(@Param("query") String query, Pageable pageable);

    /**
     * Find all contracts ordered by effective amount (COALESCE of totalAmount, taxExclusiveAmount, estimatedAmount).
     * This handles the case where we need to sort by the "real" amount displayed to users.
     * Uses NULLIF to treat zero values as NULL, so COALESCE continues to the next field.
     *
     * @param pageable pagination parameters (should not contain sort as query has its own ORDER BY)
     * @return a page of contracts ordered by effective amount
     */
    @Query(value = "SELECT c FROM Contract c ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) ASC NULLS LAST")
    Page<Contract> findAllOrderByEffectiveAmountAsc(Pageable pageable);

    @Query(value = "SELECT c FROM Contract c ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) DESC NULLS LAST")
    Page<Contract> findAllOrderByEffectiveAmountDesc(Pageable pageable);

    /**
     * Find contracts by title with amount sorting.
     */
    @Query("SELECT c FROM Contract c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) ASC NULLS LAST")
    Page<Contract> findByTitleContainingIgnoreCaseOrderByEffectiveAmountAsc(@Param("title") String title, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) DESC NULLS LAST")
    Page<Contract> findByTitleContainingIgnoreCaseOrderByEffectiveAmountDesc(@Param("title") String title, Pageable pageable);

    /**
     * Find contracts by contracting party name with amount sorting.
     */
    @Query("SELECT c FROM Contract c WHERE LOWER(c.contractingPartyName) LIKE LOWER(CONCAT('%', :contractingPartyName, '%')) ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) ASC NULLS LAST")
    Page<Contract> findByContractingPartyNameContainingIgnoreCaseOrderByEffectiveAmountAsc(@Param("contractingPartyName") String contractingPartyName, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE LOWER(c.contractingPartyName) LIKE LOWER(CONCAT('%', :contractingPartyName, '%')) ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) DESC NULLS LAST")
    Page<Contract> findByContractingPartyNameContainingIgnoreCaseOrderByEffectiveAmountDesc(@Param("contractingPartyName") String contractingPartyName, Pageable pageable);

    /**
     * Find contracts by source with amount sorting.
     */
    @Query("SELECT c FROM Contract c WHERE c.source = :source ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) ASC NULLS LAST")
    Page<Contract> findBySourceOrderByEffectiveAmountAsc(@Param("source") String source, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE c.source = :source ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) DESC NULLS LAST")
    Page<Contract> findBySourceOrderByEffectiveAmountDesc(@Param("source") String source, Pageable pageable);

    /**
     * Find contracts by country subentity with amount sorting.
     */
    @Query("SELECT c FROM Contract c WHERE LOWER(c.countrySubentity) LIKE LOWER(CONCAT('%', :countrySubentity, '%')) ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) ASC NULLS LAST")
    Page<Contract> findByCountrySubentityContainingIgnoreCaseOrderByEffectiveAmountAsc(@Param("countrySubentity") String countrySubentity, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE LOWER(c.countrySubentity) LIKE LOWER(CONCAT('%', :countrySubentity, '%')) ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) DESC NULLS LAST")
    Page<Contract> findByCountrySubentityContainingIgnoreCaseOrderByEffectiveAmountDesc(@Param("countrySubentity") String countrySubentity, Pageable pageable);

    /**
     * Global search with amount sorting.
     */
    @Query("SELECT c FROM Contract c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.contractingPartyName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) ASC NULLS LAST")
    Page<Contract> findByGlobalSearchOrderByEffectiveAmountAsc(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.contractingPartyName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) DESC NULLS LAST")
    Page<Contract> findByGlobalSearchOrderByEffectiveAmountDesc(@Param("query") String query, Pageable pageable);

    /**
     * Get comprehensive amount statistics.
     */
    @Query("SELECT " +
           "COALESCE(SUM(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 0) as totalAmount, " +
           "COALESCE(ROUND(AVG(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 2), 0) as averageAmount, " +
           "COALESCE(MAX(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 0) as maxAmount, " +
           "COALESCE(MIN(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 0) as minAmount, " +
           "SUM(CASE WHEN COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) IS NOT NULL THEN 1 ELSE 0 END) as contractsWithAmount, " +
           "SUM(CASE WHEN COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) IS NULL THEN 1 ELSE 0 END) as contractsWithoutAmount, " +
           "ROUND((SUM(CASE WHEN COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) IS NOT NULL THEN 1 ELSE 0 END) * 100.0 / COUNT(c)), 2) as amountCoverage " +
           "FROM Contract c")
    List<Object[]> getAmountStatistics();

    /**
     * Get monthly trends of contracts and amounts.
     */
    @Query(value = "SELECT " +
           "EXTRACT(YEAR FROM updated_at) as year, " +
           "EXTRACT(MONTH FROM updated_at) as month, " +
           "COUNT(*) as contractCount, " +
           "COALESCE(SUM(COALESCE(NULLIF(total_amount, 0), NULLIF(tax_exclusive_amount, 0), estimated_amount)), 0) as totalAmount " +
           "FROM contracts " +
           "WHERE updated_at IS NOT NULL " +
           "GROUP BY EXTRACT(YEAR FROM updated_at), EXTRACT(MONTH FROM updated_at) " +
           "ORDER BY year DESC, month DESC " +
           "LIMIT 24", nativeQuery = true)
    List<Object[]> getMonthlyTrends();

    /**
     * Get contract value distribution by size categories.
     */
    @Query("SELECT " +
           "CASE " +
           "   WHEN COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) < 40000 THEN 'microContracts' " +
           "   WHEN COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) < 144000 THEN 'smallContracts' " +
           "   WHEN COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) < 750000 THEN 'mediumContracts' " +
           "   ELSE 'largeContracts' " +
           "END as range, " +
           "COUNT(c) " +
           "FROM Contract c " +
           "WHERE COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) IS NOT NULL " +
           "GROUP BY range")
    List<Object[]> getContractValueDistribution();

    /**
     * Get top regions by total contract value.
     */
    @Query("SELECT " +
           "c.countrySubentity as region, " +
           "COALESCE(SUM(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 0) as totalAmount, " +
           "COUNT(c) as contractCount, " +
           "COALESCE(ROUND(AVG(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 2), 0) as averageAmount " +
           "FROM Contract c " +
           "WHERE c.countrySubentity IS NOT NULL " +
           "GROUP BY c.countrySubentity " +
           "ORDER BY totalAmount DESC")
    List<Object[]> getTopRegionsByValue();

    /**
     * Get contract type vs amount analysis.
     */
    @Query("SELECT " +
           "COALESCE(c.typeCode, 'Unknown') as typeCode, " +
           "COUNT(c) as contractCount, " +
           "COALESCE(SUM(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 0) as totalAmount, " +
           "COALESCE(ROUND(AVG(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 2), 0) as averageAmount, " +
           "COALESCE(MAX(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 0) as maxAmount " +
           "FROM Contract c " +
           "GROUP BY c.typeCode " +
           "ORDER BY totalAmount DESC")
    List<Object[]> getTypeAmountAnalysis();

    /**
     * Get source efficiency statistics.
     */
    @Query("SELECT " +
           "COALESCE(c.source, 'Unknown') as source, " +
           "COUNT(c) as contractCount, " +
           "COALESCE(SUM(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 0) as totalAmount, " +
           "COALESCE(ROUND(AVG(COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount)), 2), 0) as averageAmount, " +
           "ROUND((SUM(CASE WHEN COALESCE(NULLIF(c.totalAmount, 0), NULLIF(c.taxExclusiveAmount, 0), c.estimatedAmount) IS NOT NULL THEN 1 ELSE 0 END) * 100.0 / COUNT(c)), 2) as amountCoverage " +
           "FROM Contract c " +
           "GROUP BY c.source " +
           "ORDER BY totalAmount DESC")
    List<Object[]> getSourceEfficiencyStats();

    /**
     * Get recent activity statistics (last 30 and 7 days).
     * Uses native query since HQL doesn't support date intervals well.
     */
    @Query(value = "SELECT " +
           "SUM(CASE WHEN updated_at >= CURRENT_DATE - INTERVAL '30 days' THEN 1 ELSE 0 END) as contractsLast30Days, " +
           "SUM(CASE WHEN updated_at >= CURRENT_DATE - INTERVAL '7 days' THEN 1 ELSE 0 END) as contractsLast7Days, " +
           "COALESCE(SUM(CASE WHEN updated_at >= CURRENT_DATE - INTERVAL '30 days' THEN COALESCE(NULLIF(total_amount, 0), NULLIF(tax_exclusive_amount, 0), estimated_amount) ELSE 0 END), 0) as amountLast30Days, " +
           "COALESCE(SUM(CASE WHEN updated_at >= CURRENT_DATE - INTERVAL '7 days' THEN COALESCE(NULLIF(total_amount, 0), NULLIF(tax_exclusive_amount, 0), estimated_amount) ELSE 0 END), 0) as amountLast7Days, " +
           "ROUND(SUM(CASE WHEN updated_at >= CURRENT_DATE - INTERVAL '30 days' THEN 1 ELSE 0 END)::decimal / 30.0, 2) as avgDailyContracts " +
           "FROM contracts", nativeQuery = true)
    List<Object[]> getRecentActivityStats();

}
