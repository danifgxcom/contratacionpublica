# 📁 Scripts Directory

Esta carpeta contiene todos los scripts de automatización y análisis del proyecto.

## 🚀 Scripts de Importación

### `licitaciones_import.sh`
**Descripción**: Script principal para descargar e importar licitaciones públicas.

**Uso**:
```bash
cd scripts
./licitaciones_import.sh
```

**Funcionalidades**:
- Descarga automática de archivos ZIP desde contrataciondelsectorpublico.gob.es
- Verificación de archivos ya descargados (evita re-descargas)
- Descompresión automática de archivos .atom
- Ejecución del importador Spring Batch
- Logging detallado del proceso

**Configuración**:
- `YEAR_INICIAL=2024` - Año inicial para descarga
- Rutas relativas ajustadas para funcionar desde `scripts/`

### `descarga.sh`
**Descripción**: Script auxiliar de descarga (legacy).

## 🧪 Scripts de Testing

### `test_licitaciones.sh`
**Descripción**: Tests de validación de datos de licitaciones.

### `test_autonomous_communities.sh`
**Descripción**: Tests específicos para verificar datos de comunidades autónomas.

**Uso**:
```bash
cd scripts
./test_autonomous_communities.sh
```

## 🗄️ Scripts SQL

### `add_indexes.sql`
**Descripción**: Script para añadir índices optimizados a la base de datos.

**Uso**:
```bash
psql -d contratacionpublica -f scripts/add_indexes.sql
```

### `check_comunidades_autonomas.sql`
**Descripción**: Verificaciones de calidad de datos para comunidades autónomas.

**Queries incluidas**:
- Conteo de contratos por comunidad
- Detección de datos duplicados
- Análisis de completitud de datos

### `data_quality_analysis.sql`
**Descripción**: Análisis completo de calidad de datos.

**Funcionalidades**:
- Estadísticas generales de contratos
- Análisis de campos nulos
- Detección de inconsistencias
- Reportes de completitud

## 📋 Instrucciones de Uso

### Preparación del Entorno
```bash
# Desde el directorio raíz del proyecto
cd scripts

# Dar permisos de ejecución
chmod +x *.sh
```

### Flujo de Trabajo Típico
1. **Importar datos**: `./licitaciones_import.sh`
2. **Verificar calidad**: `./test_autonomous_communities.sh`
3. **Optimizar BD**: `psql -d contratacionpublica -f add_indexes.sql`

### Requisitos
- PostgreSQL corriendo en localhost:5432
- Base de datos `contratacionpublica` creada
- Java 17+ para el importador Spring Batch
- Conexión a internet para descarga de archivos

## 🔧 Mantenimiento

### Archivos Generados
Los scripts generan archivos en:
- `../licitaciones/` - Archivos ZIP y .atom descargados
- `../standalone-batch/` - Logs del importador

### Limpieza
```bash
# Limpiar archivos temporales
rm -rf ../licitaciones/*/
# Mantener solo los ZIP para re-procesamiento rápido
```

### Monitoreo
- Logs detallados en cada script
- Verificación automática de integridad de archivos
- Control de re-procesamiento de archivos ya importados

---

**Última actualización**: 14 Julio 2025  
**Versión**: 2.0  
**Mantenedor**: Claude AI Assistant