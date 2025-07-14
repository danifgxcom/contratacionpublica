package com.danifgx.contratacionpublica.service;

import com.danifgx.contratacionpublica.model.ContractType;
import com.danifgx.contratacionpublica.repository.ContractTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling contract type operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractTypeService {

    private final ContractTypeRepository contractTypeRepository;

    /**
     * Get all contract types.
     *
     * @return a list of all contract types
     */
    @Transactional(readOnly = true)
    public List<ContractType> getAllContractTypes() {
        return contractTypeRepository.findAll();
    }

    /**
     * Get a contract type by its code.
     *
     * @param code the code of the contract type
     * @return the contract type if found
     */
    @Transactional(readOnly = true)
    public Optional<ContractType> getContractTypeByCode(String code) {
        return contractTypeRepository.findById(code);
    }

    /**
     * Get all known contract types.
     *
     * @return a list of known contract types
     */
    @Transactional(readOnly = true)
    public List<ContractType> getKnownContractTypes() {
        return contractTypeRepository.findByKnownTrue();
    }

    /**
     * Get all unknown contract types.
     *
     * @return a list of unknown contract types
     */
    @Transactional(readOnly = true)
    public List<ContractType> getUnknownContractTypes() {
        return contractTypeRepository.findByKnownFalse();
    }

    /**
     * Get the description for a contract type code.
     * If the code is not found, it will be added as an unknown contract type.
     *
     * @param code the code of the contract type
     * @return the description of the contract type
     */
    @Transactional
    public String getDescriptionForCode(String code) {
        if (code == null || code.isEmpty()) {
            return "Desconocido";
        }

        return contractTypeRepository.findById(code)
                .map(ContractType::getDescription)
                .orElseGet(() -> {
                    // If the code is not found, add it as an unknown contract type
                    ContractType contractType = ContractType.builder()
                            .code(code)
                            .description("Tipo " + code)
                            .known(false)
                            .build();
                    contractTypeRepository.save(contractType);
                    log.info("Added unknown contract type with code: {}", code);
                    return contractType.getDescription();
                });
    }

    /**
     * Get a map of all contract type codes to their descriptions.
     *
     * @return a map of contract type codes to descriptions
     */
    @Transactional(readOnly = true)
    public Map<String, String> getContractTypeMap() {
        Map<String, String> contractTypeMap = new HashMap<>();
        contractTypeRepository.findAll().forEach(contractType -> 
            contractTypeMap.put(contractType.getCode(), contractType.getDescription())
        );
        return contractTypeMap;
    }

    /**
     * Initialize the contract types table with known values.
     * This method is called automatically when the application starts.
     */
    @PostConstruct
    @Transactional
    public void initializeContractTypes() {
        log.info("Initializing contract types...");

        // Check if contract types already exist
        if (contractTypeRepository.count() > 0) {
            log.info("Contract types already initialized, skipping...");
            return;
        }

        // Define known contract types based on the issue description
        Map<String, String> knownTypes = new HashMap<>();
        knownTypes.put("01", "Obras");
        knownTypes.put("02", "Suministros");
        knownTypes.put("03", "Servicios");
        knownTypes.put("04", "Concesión de obras");
        knownTypes.put("05", "Concesión de servicios");
        knownTypes.put("06", "Contratos mixtos");
        knownTypes.put("21", "Administrativo especial");
        knownTypes.put("22", "Sujeto a regulación armonizada");
        knownTypes.put("31", "Contrato subvencionado");
        knownTypes.put("32", "Derivado de acuerdo marco");
        knownTypes.put("50", "Asociación público-privada");
        knownTypes.put("99", "Otro o sin especificar");
        knownTypes.put("999", "No clasificado / error / legacy");

        // Save known contract types
        knownTypes.forEach((code, description) -> {
            ContractType contractType = ContractType.builder()
                    .code(code)
                    .description(description)
                    .known(true)
                    .build();
            contractTypeRepository.save(contractType);
        });

        log.info("Contract types initialized successfully");
    }
}
