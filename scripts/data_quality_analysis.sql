-- Análisis de calidad de datos para Comunidades Autónomas
-- Este script ayuda a identificar problemas en los datos

-- 1. Análisis de valores únicos en country_subentity
SELECT 
    'Valores únicos en country_subentity' as analysis_type,
    COUNT(DISTINCT country_subentity) as unique_values,
    COUNT(*) as total_records,
    COUNT(CASE WHEN country_subentity IS NULL THEN 1 END) as null_values,
    ROUND(COUNT(CASE WHEN country_subentity IS NULL THEN 1 END) * 100.0 / COUNT(*), 2) as null_percentage
FROM contracts;

-- 2. Buscar posibles variaciones en nombres (espacios, mayúsculas, acentos)
WITH cleaned_names AS (
    SELECT 
        country_subentity,
        TRIM(UPPER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
            country_subentity, 'á', 'A'), 'é', 'E'), 'í', 'I'), 'ó', 'O'), 'ú', 'U'))) as normalized,
        COUNT(*) as count
    FROM contracts 
    WHERE country_subentity IS NOT NULL 
    GROUP BY country_subentity
)
SELECT 
    normalized,
    COUNT(*) as variant_count,
    STRING_AGG(country_subentity || ' (' || count || ')', ', ') as variants
FROM cleaned_names 
GROUP BY normalized 
HAVING COUNT(*) > 1
ORDER BY variant_count DESC;

-- 3. Verificar correspondencia entre NUTS codes y comunidades autónomas
SELECT 
    SUBSTRING(nuts_code, 1, 3) as nuts_region,
    country_subentity,
    COUNT(*) as count
FROM contracts 
WHERE nuts_code IS NOT NULL AND country_subentity IS NOT NULL
GROUP BY SUBSTRING(nuts_code, 1, 3), country_subentity
ORDER BY nuts_region, count DESC;

-- 4. Identificar registros con inconsistencias geográficas
SELECT 
    'Inconsistencias geográficas' as issue_type,
    nuts_code,
    country_subentity,
    COUNT(*) as count
FROM contracts 
WHERE nuts_code IS NOT NULL AND country_subentity IS NOT NULL
  AND (
    (nuts_code LIKE 'ES1%' AND country_subentity NOT IN ('Galicia', 'Asturias', 'Cantabria', 'País Vasco', 'Navarra', 'La Rioja', 'Aragón', 'Cataluña')) OR
    (nuts_code LIKE 'ES2%' AND country_subentity NOT IN ('Castilla y León', 'Madrid', 'Castilla-La Mancha', 'Extremadura')) OR
    (nuts_code LIKE 'ES3%' AND country_subentity NOT IN ('Valencia', 'Murcia')) OR
    (nuts_code LIKE 'ES4%' AND country_subentity NOT IN ('Andalucía')) OR
    (nuts_code LIKE 'ES5%' AND country_subentity NOT IN ('Canarias')) OR
    (nuts_code LIKE 'ES6%' AND country_subentity NOT IN ('Baleares')) OR
    (nuts_code LIKE 'ES7%' AND country_subentity NOT IN ('Ceuta', 'Melilla'))
  )
GROUP BY nuts_code, country_subentity
ORDER BY count DESC;

-- 5. Análisis temporal por comunidad autónoma
SELECT 
    country_subentity,
    COUNT(*) as total_contracts,
    MIN(updated_at) as earliest_date,
    MAX(updated_at) as latest_date,
    EXTRACT(DAYS FROM MAX(updated_at) - MIN(updated_at)) as date_range_days
FROM contracts 
WHERE country_subentity IS NOT NULL AND updated_at IS NOT NULL
GROUP BY country_subentity
ORDER BY total_contracts DESC;

-- 6. Análisis de completitud de datos por comunidad autónoma
SELECT 
    country_subentity,
    COUNT(*) as total_contracts,
    COUNT(total_amount) as contracts_with_total_amount,
    COUNT(tax_exclusive_amount) as contracts_with_tax_exclusive_amount,
    COUNT(estimated_amount) as contracts_with_estimated_amount,
    COUNT(CASE WHEN COALESCE(total_amount, tax_exclusive_amount, estimated_amount) IS NOT NULL THEN 1 END) as contracts_with_any_amount,
    ROUND(COUNT(CASE WHEN COALESCE(total_amount, tax_exclusive_amount, estimated_amount) IS NOT NULL THEN 1 END) * 100.0 / COUNT(*), 2) as amount_completeness_pct
FROM contracts 
WHERE country_subentity IS NOT NULL
GROUP BY country_subentity
ORDER BY total_contracts DESC;