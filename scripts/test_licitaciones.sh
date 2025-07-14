#!/bin/bash

# Script de prueba para verificar la funcionalidad de licitaciones_import.sh
# Este script solo comprueba un pequeño subconjunto de archivos para verificar que todo funciona correctamente

# Guardar el script original
cp licitaciones_import.sh licitaciones_import.sh.bak

# Modificar el script para probar solo con un año y mes específicos
sed -i 's/YEAR_INICIAL=2024/YEAR_INICIAL=2024/' licitaciones_import.sh
sed -i 's/for mes in $(seq -w 1 12)/for mes in $(seq -w 4 5)/' licitaciones_import.sh

# Ejecutar el script modificado
./licitaciones_import.sh

# Restaurar el script original
mv licitaciones_import.sh.bak licitaciones_import.sh

echo "Prueba completada. Verifique los resultados."