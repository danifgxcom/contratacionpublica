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

}
