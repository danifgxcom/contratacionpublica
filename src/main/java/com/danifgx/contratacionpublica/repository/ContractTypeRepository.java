package com.danifgx.contratacionpublica.repository;

import com.danifgx.contratacionpublica.model.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ContractType entity.
 */
@Repository
public interface ContractTypeRepository extends JpaRepository<ContractType, String> {

    /**
     * Find all known contract types.
     *
     * @return a list of known contract types
     */
    List<ContractType> findByKnownTrue();

    /**
     * Find all unknown contract types.
     *
     * @return a list of unknown contract types
     */
    List<ContractType> findByKnownFalse();
}