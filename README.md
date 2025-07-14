# Contratación Pública

Este proyecto consta de dos componentes principales:

1. **API REST (Spring Boot)**: Una aplicación Spring Boot que proporciona una API REST para consultar información sobre contratos públicos.
2. **Importador de Archivos Atom (Standalone)**: Una aplicación Spring Batch independiente para importar archivos atom de contratos públicos a una base de datos PostgreSQL.

## Estructura del Proyecto

- **src/main/java/com/danifgx/contratacionpublica**: Código fuente de la aplicación Spring Boot (API REST)
- **standalone-batch**: Aplicación independiente para importar archivos atom

## API REST (Spring Boot)

La aplicación Spring Boot proporciona una API REST para consultar información sobre contratos públicos almacenados en la base de datos. Esta aplicación no contiene ninguna funcionalidad de procesamiento por lotes (batch processing), ya que esa funcionalidad ha sido extraída a una aplicación independiente (ver sección "Importador de Archivos Atom").

### Endpoints Principales

- `GET /api/contracts`: Obtiene todos los contratos con paginación
- `GET /api/contracts/{id}`: Obtiene un contrato por su ID
- `GET /api/contracts/search/title`: Busca contratos por título
- `GET /api/contracts/search/contracting-party`: Busca contratos por nombre de la parte contratante
- `GET /api/contracts/statistics`: Obtiene estadísticas sobre los contratos
- `GET /api/contracts/count`: Obtiene el número total de contratos

### Cómo Ejecutar la API REST

```bash
# Compilar la aplicación
mvn clean package

# Ejecutar la aplicación
java -jar target/contratacionpublica-0.0.1-SNAPSHOT.jar
```

### Cambios Recientes

Se ha eliminado toda la funcionalidad de procesamiento por lotes (batch processing) de la aplicación Spring Boot, ya que esta funcionalidad ahora está disponible en la aplicación independiente "Importador de Archivos Atom". Esto permite que la aplicación Spring Boot se centre exclusivamente en proporcionar una API REST para consultar información sobre contratos públicos.

## Importador de Archivos Atom (Standalone)

La aplicación standalone procesa archivos atom que contienen información de contratos y los importa a la base de datos PostgreSQL. El importador mantiene un registro de los archivos ya procesados para evitar procesar el mismo archivo más de una vez.

### Script de Descarga e Importación Automática

El proyecto incluye un script `licitaciones_import.sh` que automatiza el proceso de descarga y procesamiento de archivos:

```bash
# Dar permisos de ejecución al script
chmod +x licitaciones_import.sh

# Ejecutar el script
./licitaciones_import.sh
```

Este script realiza las siguientes acciones:
1. Verifica qué archivos están disponibles en el portal de contratación del sector público
2. Comprueba qué archivos ya han sido descargados previamente
3. Calcula la diferencia y descarga solo los archivos nuevos o pendientes
4. Muestra el porcentaje de progreso durante la descarga y procesamiento
5. Descomprime los archivos en directorios individuales dentro de `./licitaciones`
6. Ejecuta automáticamente el importador para procesar los archivos

El script está diseñado para ser eficiente:
- No vuelve a descargar archivos que ya existen
- No descomprime carpetas que ya están descomprimidas
- Muestra el progreso en tiempo real con porcentajes
- Procesa todos los archivos descargados al final

### Cómo Ejecutar el Importador Manualmente

Hay dos formas de lanzar la aplicación manualmente:

#### 1. Usando el script de construcción y ejecución

El método más sencillo es utilizar el script `build-and-run.sh` incluido en el directorio `standalone-batch`:

```bash
cd standalone-batch
chmod +x build-and-run.sh
./build-and-run.sh
```

Este script realizará las siguientes acciones:
1. Compilar la aplicación con Maven
2. Crear un JAR ejecutable con todas las dependencias
3. Ejecutar la aplicación

#### 2. Manualmente

Si prefieres ejecutar los comandos manualmente:

```bash
# Navegar al directorio standalone-batch
cd standalone-batch

# Compilar la aplicación
mvn clean package

# Ejecutar la aplicación
java -jar target/atom-importer-1.0.0-jar-with-dependencies.jar
```

### Configuración del Importador

La aplicación está configurada mediante el archivo `application.properties` ubicado en `standalone-batch/src/main/resources`. Las principales propiedades son:

- `spring.datasource.url`: URL JDBC de la base de datos
- `spring.datasource.username`: Usuario de la base de datos
- `spring.datasource.password`: Contraseña de la base de datos
- `app.file.input-directory`: Directorio que contiene los archivos atom a procesar
- `app.file.processed-directory`: Directorio donde se moverán los archivos procesados

Por defecto, la aplicación buscará archivos atom en el directorio `./licitaciones` y moverá los archivos procesados a `./licitaciones/processed`.

### Seguimiento de Archivos Procesados

El importador mantiene un registro de los archivos que ya han sido procesados en la tabla `processed_files` de la base de datos. Esto permite que el sistema evite procesar el mismo archivo más de una vez, incluso si se ejecuta el importador múltiples veces sobre el mismo directorio de entrada.

Cuando se ejecuta el importador, este:
1. Busca todos los archivos .atom en el directorio de entrada y sus subdirectorios
2. Consulta la base de datos para obtener la lista de archivos ya procesados
3. Procesa solo los archivos que no han sido procesados anteriormente
4. Registra los archivos procesados en la base de datos para futuras ejecuciones

## Requisitos

- Java 11 o superior
- Maven
- Base de datos PostgreSQL
