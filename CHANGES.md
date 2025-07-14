# Contract Type Mapping Implementation

## Overview
This implementation adds support for mapping contract type codes to their human-readable descriptions. Contract types are stored in a new `contract_types` table in the database, and the application provides APIs for managing and retrieving contract types.

## Changes Made

### Database Changes
- Created a new `contract_types` table with the following columns:
  - `code` (VARCHAR): The contract type code (e.g., "01", "02", "03")
  - `description` (VARCHAR): The human-readable description (e.g., "Obras", "Suministros", "Servicios")
  - `is_known` (BOOLEAN): Flag indicating whether this is a known contract type

### Model Changes
- Created a new `ContractType` entity class to represent contract types in the application

### Repository Changes
- Created a new `ContractTypeRepository` interface for accessing contract types in the database

### Service Changes
- Created a new `ContractTypeService` class for managing contract types
- Added methods for retrieving, creating, and updating contract types
- Implemented automatic initialization of known contract types
- Updated `ContractService` to use `ContractTypeService` for mapping contract type codes to descriptions
- Enhanced contract statistics to include contract type descriptions

### Controller Changes
- Created a new `ContractTypeController` class for exposing contract type APIs
- Added endpoints for retrieving contract types and their descriptions

### API Endpoints
- `GET /api/contract-types`: Get all contract types
- `GET /api/contract-types/{code}`: Get a contract type by its code
- `GET /api/contract-types/known`: Get all known contract types
- `GET /api/contract-types/unknown`: Get all unknown contract types
- `GET /api/contract-types/map`: Get a map of all contract type codes to their descriptions

## Known Contract Types
The following contract types are pre-configured in the system:
- "01": "Obras"
- "02": "Suministros"
- "03": "Servicios"
- "04": "Concesión de obras"
- "05": "Concesión de servicios"
- "06": "Contratos mixtos"
- "21": "Administrativo especial"
- "22": "Sujeto a regulación armonizada"
- "31": "Contrato subvencionado"
- "32": "Derivado de acuerdo marco"
- "50": "Asociación público-privada"
- "99": "Otro o sin especificar"
- "999": "No clasificado / error / legacy"

## Handling Unknown Contract Types
If a contract has a type code that is not in the pre-configured list, the system will:
1. Automatically add it to the `contract_types` table with `is_known` set to `false`
2. Assign a default description of "Tipo X" where X is the code
3. Include it in the statistics with this default description

This allows the system to handle all contract types, even if they are not in the pre-configured list, while still providing a way to distinguish between known and unknown types.