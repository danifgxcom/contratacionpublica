#!/bin/bash

# Script para probar las consultas de Comunidades Autónomas
# Asegúrate de que PostgreSQL esté corriendo y la base de datos exista

DB_NAME="contratacionpublica"
DB_USER="postgres"
DB_HOST="localhost"
DB_PORT="5432"

echo "=== Análisis de Comunidades Autónomas en Contratación Pública ==="
echo "Conectando a la base de datos: $DB_NAME"
echo ""

# Verificar conexión
echo "1. Verificando conexión a la base de datos..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "\dt" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Conexión exitosa a la base de datos"
else
    echo "✗ Error: No se pudo conectar a la base de datos"
    echo "Asegúrate de que PostgreSQL esté corriendo y las credenciales sean correctas"
    exit 1
fi

echo ""
echo "2. Ejecutando análisis de calidad de datos..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f data_quality_analysis.sql

echo ""
echo "3. Ejecutando consulta de estadísticas por Comunidad Autónoma..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f check_comunidades_autonomas.sql

echo ""
echo "4. Probando la nueva consulta de estadísticas agregadas..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
SELECT 
    COALESCE(country_subentity, 'Unknown') as autonomous_community,
    COUNT(*) as contract_count,
    COALESCE(SUM(COALESCE(total_amount, tax_exclusive_amount, estimated_amount)), 0) as total_amount,
    ROUND(AVG(COALESCE(total_amount, tax_exclusive_amount, estimated_amount)), 2) as average_amount
FROM contracts 
GROUP BY country_subentity 
ORDER BY COUNT(*) DESC 
LIMIT 10;
"

echo ""
echo "=== Análisis completado ==="