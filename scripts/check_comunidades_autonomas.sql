-- Consultas para analizar el campo country_subentity (Comunidades Autónomas)

-- 1. Verificar datos distintos en country_subentity
SELECT DISTINCT country_subentity, COUNT(*) as count
FROM contracts 
WHERE country_subentity IS NOT NULL 
GROUP BY country_subentity 
ORDER BY count DESC;

-- 2. Verificar posibles duplicados o inconsistencias (variaciones en mayúsculas/minúsculas o espacios)
SELECT 
    TRIM(UPPER(country_subentity)) as normalized,
    country_subentity as original,
    COUNT(*) as count
FROM contracts 
WHERE country_subentity IS NOT NULL 
GROUP BY TRIM(UPPER(country_subentity)), country_subentity
HAVING COUNT(DISTINCT country_subentity) > 1
ORDER BY normalized, count DESC;

-- 3. Estadísticas básicas por Comunidad Autónoma
SELECT 
    country_subentity,
    COUNT(*) as total_contracts,
    COUNT(CASE WHEN total_amount IS NOT NULL THEN 1 END) as contracts_with_amount,
    ROUND(AVG(COALESCE(total_amount, tax_exclusive_amount, estimated_amount)), 2) as avg_amount,
    ROUND(SUM(COALESCE(total_amount, tax_exclusive_amount, estimated_amount)), 2) as total_amount_sum,
    MIN(updated_at) as earliest_contract,
    MAX(updated_at) as latest_contract
FROM contracts 
WHERE country_subentity IS NOT NULL 
GROUP BY country_subentity 
ORDER BY total_contracts DESC;

-- 4. Verificar correspondencia entre nuts_code y country_subentity
SELECT 
    nuts_code,
    country_subentity,
    COUNT(*) as count
FROM contracts 
WHERE nuts_code IS NOT NULL AND country_subentity IS NOT NULL
GROUP BY nuts_code, country_subentity
ORDER BY nuts_code, count DESC;

-- 5. Buscar posibles inconsistencias en los datos
SELECT 
    'NULL country_subentity but has nuts_code' as issue_type,
    nuts_code,
    COUNT(*) as count
FROM contracts 
WHERE country_subentity IS NULL AND nuts_code IS NOT NULL
GROUP BY nuts_code

UNION ALL

SELECT 
    'NULL nuts_code but has country_subentity' as issue_type,
    country_subentity,
    COUNT(*) as count
FROM contracts 
WHERE nuts_code IS NULL AND country_subentity IS NOT NULL
GROUP BY country_subentity

ORDER BY issue_type, count DESC;