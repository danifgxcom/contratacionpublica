# Análisis de Estadísticas por Comunidad Autónoma

## Resumen del Análisis

He realizado una búsqueda exhaustiva en el código backend para identificar la implementación de estadísticas por Comunidad Autónoma y he encontrado lo siguiente:

## Estado Actual de la Implementación

### ✅ Lo que está implementado:

1. **Campo en el modelo**: La entidad `Contract` tiene el campo `countrySubentity` que almacena las Comunidades Autónomas
2. **Query para regiones distintas**: Existe una consulta en `ContractRepository.findDistinctRegions()` que obtiene regiones únicas
3. **Endpoint para regiones**: El controlador expone `/api/contracts/regions` para obtener las regiones

### ❌ Lo que faltaba:

1. **Query de estadísticas agrupadas**: No existía una consulta que agrupe contratos por Comunidad Autónoma
2. **Inclusión en estadísticas generales**: Las estadísticas por CA no estaban incluidas en el endpoint principal
3. **Endpoint específico**: No había un endpoint dedicado para estadísticas por Comunidad Autónoma

## Mejoras Implementadas

### 1. Repository (`ContractRepository.java`)

He agregado el método `countByAutonomousCommunity()`:

```java
@Query("SELECT COALESCE(c.countrySubentity, 'Unknown'), COUNT(c), " +
       "COALESCE(SUM(COALESCE(c.totalAmount, c.taxExclusiveAmount, c.estimatedAmount)), 0), " +
       "ROUND(AVG(COALESCE(c.totalAmount, c.taxExclusiveAmount, c.estimatedAmount)), 2) " +
       "FROM Contract c " +
       "GROUP BY c.countrySubentity " +
       "ORDER BY COUNT(c) DESC")
List<Object[]> countByAutonomousCommunity();
```

Esta consulta devuelve:
- Nombre de la Comunidad Autónoma
- Número total de contratos
- Importe total de los contratos
- Importe promedio de los contratos

### 2. Service (`ContractService.java`)

He modificado el método `getContractStatistics()` para incluir las estadísticas por CA y agregado un nuevo método específico:

```java
public List<Map<String, Object>> getStatisticsByAutonomousCommunity()
```

### 3. Controller (`ContractController.java`)

He agregado un nuevo endpoint:

```java
@GetMapping("/statistics/autonomous-communities")
public ResponseEntity<List<Map<String, Object>>> getStatisticsByAutonomousCommunity()
```

### 4. Tests (`ContractServiceTest.java`)

He actualizado los tests para verificar que las estadísticas por CA se incluyan correctamente en la respuesta.

## Análisis de Calidad de Datos

He creado scripts SQL para verificar la calidad de los datos:

### 1. `check_comunidades_autonomas.sql`
Verifica:
- Datos distintos en `country_subentity`
- Posibles duplicados o inconsistencias
- Estadísticas básicas por CA
- Correspondencia entre `nuts_code` y `country_subentity`
- Inconsistencias en los datos

### 2. `data_quality_analysis.sql`
Analiza:
- Valores únicos y nulos
- Variaciones en nombres (espacios, mayúsculas, acentos)
- Inconsistencias geográficas
- Análisis temporal
- Completitud de datos por CA

### 3. `test_autonomous_communities.sh`
Script ejecutable para probar todas las consultas en PostgreSQL.

## Estructura de Datos

### Campo principal: `country_subentity`
- **Tipo**: String
- **Descripción**: Nombre de la Comunidad Autónoma
- **Posibles valores**: "Madrid", "Cataluña", "Andalucía", etc.
- **Valores NULL**: Se tratan como "Unknown" en las estadísticas

### Relación con NUTS codes:
- Campo `nuts_code`: Código NUTS de la ubicación
- Campo `country_subentity`: Nombre legible de la CA
- Ambos campos están relacionados geográficamente

## Endpoints Disponibles

1. **Estadísticas generales** (incluye CA):
   ```
   GET /api/contracts/statistics
   ```

2. **Estadísticas específicas por CA**:
   ```
   GET /api/contracts/statistics/autonomous-communities
   ```

3. **Regiones distintas**:
   ```
   GET /api/contracts/regions
   ```

## Posibles Problemas Identificados

1. **Inconsistencias en nombres**: Variaciones en mayúsculas, espacios, acentos
2. **Datos faltantes**: Contratos sin `country_subentity`
3. **Correspondencia NUTS**: Posible desalineación entre códigos NUTS y nombres de CA
4. **Duplicados**: Nombres similares pero escritos de forma diferente

## Recomendaciones

1. **Ejecutar los scripts de análisis** para identificar problemas reales en los datos
2. **Normalizar nombres** de Comunidades Autónomas
3. **Validar correspondencia** entre NUTS codes y nombres de CA
4. **Implementar validación** en el proceso de importación
5. **Crear mapping estático** de códigos NUTS a nombres de CA para consistencia

## Archivos Modificados

- `/home/danifgx/projects/contratacionpublica/src/main/java/com/danifgx/contratacionpublica/repository/ContractRepository.java`
- `/home/danifgx/projects/contratacionpublica/src/main/java/com/danifgx/contratacionpublica/service/ContractService.java`
- `/home/danifgx/projects/contratacionpublica/src/main/java/com/danifgx/contratacionpublica/controller/ContractController.java`
- `/home/danifgx/projects/contratacionpublica/src/test/java/com/danifgx/contratacionpublica/service/ContractServiceTest.java`

## Archivos Creados

- `/home/danifgx/projects/contratacionpublica/check_comunidades_autonomas.sql`
- `/home/danifgx/projects/contratacionpublica/data_quality_analysis.sql`
- `/home/danifgx/projects/contratacionpublica/test_autonomous_communities.sh`
- `/home/danifgx/projects/contratacionpublica/ANALISIS_COMUNIDADES_AUTONOMAS.md`