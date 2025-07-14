#!/bin/bash

# Desactivar modo de depuración para una salida más limpia
# set -x

BASE_DIR="../licitaciones"
mkdir -p "$BASE_DIR"

# Verificar que el directorio se creó correctamente
if [ ! -d "$BASE_DIR" ]; then
  echo "❌ Error: No se pudo crear el directorio $BASE_DIR"
  exit 1
fi

EXCLUIDO=""
# Ajustar el año inicial a 2024 para archivos más recientes
YEAR_INICIAL=2024
YEAR_ACTUAL=$(date +"%Y")
MES_ACTUAL=$(date +"%m")

echo "🔍 Configuración: Año inicial=$YEAR_INICIAL, Año actual=$YEAR_ACTUAL, Mes actual=$MES_ACTUAL"

# Función para verificar si un archivo existe en la web
check_file_exists() {
  local url=$1
  local http_code=$(curl -s -o /dev/null -w "%{http_code}" --head "$url")
  if [[ "$http_code" == "200" || "$http_code" == "302" ]]; then
    return 0  # Archivo existe
  else
    return 1  # Archivo no existe
  fi
}

# Arrays para almacenar información
declare -a AVAILABLE_FILES=()
declare -a AVAILABLE_URLS=()
declare -a AVAILABLE_PREFIXES=()
declare -a AVAILABLE_DIRS=()
declare -a DOWNLOADED_FILES=()
declare -a TO_DOWNLOAD_FILES=()
declare -a TO_DOWNLOAD_URLS=()
declare -a TO_DOWNLOAD_PREFIXES=()
declare -a TO_DOWNLOAD_DIRS=()

echo "🔍 Buscando archivos disponibles en la web..."

# Primero, verificar qué archivos están disponibles en la web
TOTAL_COMBINATIONS=0
AVAILABLE_COUNT=0

# Calcular el número total de combinaciones posibles
for year in $(seq $YEAR_INICIAL $YEAR_ACTUAL); do
  for mes in $(seq -w 1 12); do
    [ "$year" = "$YEAR_ACTUAL" ] && [ "$mes" -gt "$MES_ACTUAL" ] && break
    YMM="${year}${mes}"
    [ "$YMM" = "$EXCLUIDO" ] && continue

    for entrada in "643:perfiles:licitacionesPerfilesContratanteCompleto3" \
                   "1044:agregadas:PlataformasAgregadasSinMenores"; do
      TOTAL_COMBINATIONS=$((TOTAL_COMBINATIONS + 1))
    done
  done
done

echo "📊 Total de combinaciones a verificar: $TOTAL_COMBINATIONS"

# Verificar cada combinación
CHECKED=0
for year in $(seq $YEAR_INICIAL $YEAR_ACTUAL); do
  for mes in $(seq -w 1 12); do
    [ "$year" = "$YEAR_ACTUAL" ] && [ "$mes" -gt "$MES_ACTUAL" ] && break
    YMM="${year}${mes}"
    [ "$YMM" = "$EXCLUIDO" ] && continue

    for entrada in "643:perfiles:licitacionesPerfilesContratanteCompleto3" \
                   "1044:agregadas:PlataformasAgregadasSinMenores"; do
      ID="${entrada%%:*}"
      REST="${entrada#*:}"
      PREFIJO="${REST%%:*}"
      NOMBRE="${REST#*:}"

      URL="https://contrataciondelsectorpublico.gob.es/sindicacion/sindicacion_${ID}/${NOMBRE}_${YMM}.zip"
      ZIP_PATH="${BASE_DIR}/${PREFIJO}_${YMM}.zip"
      DEST_DIR="${BASE_DIR}/${PREFIJO}_${YMM}"

      CHECKED=$((CHECKED + 1))
      PERCENT=$((CHECKED * 100 / TOTAL_COMBINATIONS))
      echo -ne "🔍 Verificando disponibilidad: $PERCENT% completado ($CHECKED/$TOTAL_COMBINATIONS)\r"

      # Verificar si el archivo existe en la web
      if check_file_exists "$URL"; then
        AVAILABLE_FILES+=("$ZIP_PATH")
        AVAILABLE_URLS+=("$URL")
        AVAILABLE_PREFIXES+=("$PREFIJO")
        AVAILABLE_DIRS+=("$DEST_DIR")
        AVAILABLE_COUNT=$((AVAILABLE_COUNT + 1))
      fi
    done
  done
done

echo -e "\n✅ Verificación completada. Se encontraron $AVAILABLE_COUNT archivos disponibles en la web."

# Verificar qué archivos ya están descargados
echo "🔍 Verificando archivos ya descargados..."
DOWNLOADED_COUNT=0

for i in "${!AVAILABLE_FILES[@]}"; do
  ZIP_PATH="${AVAILABLE_FILES[$i]}"

  if [ -f "$ZIP_PATH" ] && [ -s "$ZIP_PATH" ] && file "$ZIP_PATH" | grep -q "Zip archive data"; then
    DOWNLOADED_FILES+=("$ZIP_PATH")
    DOWNLOADED_COUNT=$((DOWNLOADED_COUNT + 1))
  else
    TO_DOWNLOAD_FILES+=("$ZIP_PATH")
    TO_DOWNLOAD_URLS+=("${AVAILABLE_URLS[$i]}")
    TO_DOWNLOAD_PREFIXES+=("${AVAILABLE_PREFIXES[$i]}")
    TO_DOWNLOAD_DIRS+=("${AVAILABLE_DIRS[$i]}")
  fi
done

echo "📊 Archivos ya descargados: $DOWNLOADED_COUNT de $AVAILABLE_COUNT"
echo "📊 Archivos pendientes de descarga: ${#TO_DOWNLOAD_FILES[@]}"

# Descargar los archivos pendientes
if [ ${#TO_DOWNLOAD_FILES[@]} -gt 0 ]; then
  echo "📥 Iniciando descarga de archivos pendientes..."

  for i in "${!TO_DOWNLOAD_FILES[@]}"; do
    ZIP_PATH="${TO_DOWNLOAD_FILES[$i]}"
    URL="${TO_DOWNLOAD_URLS[$i]}"
    PERCENT=$(( (i + 1) * 100 / ${#TO_DOWNLOAD_FILES[@]} ))

    echo -ne "📥 Descargando: $PERCENT% completado ($(($i + 1))/${#TO_DOWNLOAD_FILES[@]})\r"
    echo "📥 Descargando $URL..."

    # Usar curl con opciones para mejor diagnóstico
    curl -s -L --retry 3 --connect-timeout 30 -o "$ZIP_PATH" "$URL"

    # Verificar si el archivo descargado es un ZIP válido
    if [ -f "$ZIP_PATH" ] && [ -s "$ZIP_PATH" ] && file "$ZIP_PATH" | grep -q "Zip archive data"; then
      echo "✅ Guardado $ZIP_PATH"
      DOWNLOADED_FILES+=("$ZIP_PATH")
    else
      echo "❌ Error al descargar: $URL"
      # Si el archivo existe pero está vacío o es inválido, eliminarlo
      if [ -f "$ZIP_PATH" ]; then
        echo "🗑️ Eliminando archivo inválido: $ZIP_PATH"
        rm "$ZIP_PATH"
      fi
    fi
  done

  echo -e "\n✅ Proceso de descarga completado."
else
  echo "✅ No hay archivos pendientes de descarga."
fi

# Descomprimir todos los archivos descargados
echo "📦 Iniciando descompresión de archivos..."
DECOMPRESS_COUNT=0
TOTAL_TO_DECOMPRESS=${#DOWNLOADED_FILES[@]}

for ZIP_PATH in "${DOWNLOADED_FILES[@]}"; do
  # Extraer el prefijo y el YMM del nombre del archivo
  FILENAME=$(basename "$ZIP_PATH")
  PREFIJO=$(echo "$FILENAME" | cut -d'_' -f1)
  YMM=$(echo "$FILENAME" | cut -d'_' -f2 | cut -d'.' -f1)
  DEST_DIR="${BASE_DIR}/${PREFIJO}_${YMM}"

  DECOMPRESS_COUNT=$((DECOMPRESS_COUNT + 1))
  PERCENT=$((DECOMPRESS_COUNT * 100 / TOTAL_TO_DECOMPRESS))
  echo -ne "📦 Descomprimiendo: $PERCENT% completado ($DECOMPRESS_COUNT/$TOTAL_TO_DECOMPRESS)\r"

  # Descomprimir si no está hecho aún
  if [ -f "$ZIP_PATH" ] && [ -s "$ZIP_PATH" ]; then
    if [ ! -d "$DEST_DIR" ]; then
      echo "📦 Descomprimiendo en $DEST_DIR..."
      mkdir -p "$DEST_DIR"

      # Descomprimir con mejor manejo de errores
      if unzip -t "$ZIP_PATH" > /dev/null 2>&1; then
        echo "✅ Verificación del ZIP correcta, descomprimiendo..."
        unzip -q -o "$ZIP_PATH" -d "$DEST_DIR" && echo "✅ Descomprimido $ZIP_PATH en $DEST_DIR"

        # Verificar que se descomprimieron archivos
        FILE_COUNT=$(find "$DEST_DIR" -type f | wc -l)
        echo "📊 Se descomprimieron $FILE_COUNT archivos en $DEST_DIR"
      else
        echo "❌ Error: El archivo ZIP está corrupto o no es válido"
        rm -f "$ZIP_PATH"
        echo "🗑️ Se eliminó el archivo ZIP corrupto"
      fi
    else
      echo "📂 Ya descomprimido: $DEST_DIR"
      # Verificar que el directorio contiene archivos
      FILE_COUNT=$(find "$DEST_DIR" -type f | wc -l)
      echo "📊 El directorio contiene $FILE_COUNT archivos"
    fi
  else
    echo "⚠️ No se puede descomprimir: El archivo ZIP no existe o está vacío"
  fi
done

echo -e "\n✅ Proceso de descompresión completado."

# Verificar si se descargó al menos un archivo
DOWNLOADED_ZIP_COUNT=$(find "$BASE_DIR" -name "*.zip" | wc -l)
echo "📊 Total de archivos ZIP descargados: $DOWNLOADED_ZIP_COUNT"

# Verificar si se encontraron archivos .atom
ATOM_FILES=$(find "$BASE_DIR" -name "*.atom" | wc -l)
echo "📊 Total de archivos .atom encontrados: $ATOM_FILES"

if [ "$ATOM_FILES" -eq 0 ]; then
  echo "⚠️ Advertencia: No se encontraron archivos .atom para procesar"
  echo "🔍 Compruebe la conexión a internet y que las URLs sean correctas"
  echo "🔍 URLs de ejemplo:"
  echo "   - https://contrataciondelsectorpublico.gob.es/sindicacion/sindicacion_643/licitacionesPerfilesContratanteCompleto3_202301.zip"
  echo "   - https://contrataciondelsectorpublico.gob.es/sindicacion/sindicacion_1044/PlataformasAgregadasSinMenores_202301.zip"
else
  # Ejecutar el importador de Spring Batch
  echo "🚀 Ejecutando el importador de Spring Batch..."

  if [ -d "../standalone-batch" ]; then
    cd ../standalone-batch

    if [ -f "build-and-run.sh" ] && [ -x "build-and-run.sh" ]; then
      ./build-and-run.sh
      BATCH_RESULT=$?

      if [ $BATCH_RESULT -eq 0 ]; then
        echo "✅ Proceso de importación completado exitosamente"
      else
        echo "❌ Error en el proceso de importación (código: $BATCH_RESULT)"
      fi
    else
      echo "❌ Error: El script build-and-run.sh no existe o no tiene permisos de ejecución"
      echo "🔧 Ejecute: chmod +x standalone-batch/build-and-run.sh"
    fi

    cd ../scripts
  else
    echo "❌ Error: El directorio standalone-batch no existe"
    echo "🔍 Verifique que está ejecutando el script desde el directorio correcto"
  fi
fi

echo "🏁 Script finalizado"
