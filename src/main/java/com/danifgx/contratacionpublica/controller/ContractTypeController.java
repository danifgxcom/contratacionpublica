package com.danifgx.contratacionpublica.controller;

import com.danifgx.contratacionpublica.model.ContractType;
import com.danifgx.contratacionpublica.service.ContractTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for contract type operations.
 */
@RestController
@RequestMapping("/api/contract-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContractTypeController {

    private final ContractTypeService contractTypeService;

    /**
     * Get all contract types.
     *
     * @return a list of all contract types
     */
    @GetMapping
    public ResponseEntity<List<ContractType>> getAllContractTypes() {
        return ResponseEntity.ok(contractTypeService.getAllContractTypes());
    }

    /**
     * Get a contract type by its code.
     *
     * @param code the code of the contract type
     * @return the contract type if found
     */
    @GetMapping("/{code}")
    public ResponseEntity<ContractType> getContractTypeByCode(@PathVariable String code) {
        return contractTypeService.getContractTypeByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all known contract types.
     *
     * @return a list of known contract types
     */
    @GetMapping("/known")
    public ResponseEntity<List<ContractType>> getKnownContractTypes() {
        return ResponseEntity.ok(contractTypeService.getKnownContractTypes());
    }

    /**
     * Get all unknown contract types.
     *
     * @return a list of unknown contract types
     */
    @GetMapping("/unknown")
    public ResponseEntity<List<ContractType>> getUnknownContractTypes() {
        return ResponseEntity.ok(contractTypeService.getUnknownContractTypes());
    }

    /**
     * Get a map of all contract type codes to their descriptions.
     *
     * @return a map of contract type codes to descriptions
     */
    @GetMapping("/map")
    public ResponseEntity<Map<String, String>> getContractTypeMap() {
        return ResponseEntity.ok(contractTypeService.getContractTypeMap());
    }
}