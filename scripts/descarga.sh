#!/bin/bash

# Carpeta raíz
BASE_DIR="./licitaciones"
mkdir -p "$BASE_DIR"

# Fechas
AÑO_INICIO=2024
AÑO_ACTUAL=$(date +"%Y")
MES_ACTUAL=$(date +"%m")
EXCLUIDO="202507"

# Declaramos las fuentes: ID_sindicacion, prefijo carpeta, nombre fichero
FUENTES=(
  "643|perfiles|licitacionesPerfilesContratanteCompleto3"
  "1044|agregadas|PlataformasAgregadasSinMenores"
)

# Recorremos año y mes
for AÑO in $(seq $AÑO_INICIO $AÑO_ACTUAL); do
  for MES in $(seq -w 1 12); do
    [[ "$AÑO" == "$AÑO_ACTUAL" && "$MES" -gt "$MES_ACTUAL" ]] && break

    YMM="${AÑO}${MES}"
    [[ "$YMM" == "$EXCLUIDO" ]] && continue

    for FUENTE in "${FUENTES[@]}"; do
      IFS="|" read -r ID PREFIJO NOMBRE <<< "$FUENTE"

      ZIP_FILE="${PREFIJO}_${YMM}.zip"
      DEST_ZIP="${BASE_DIR}/${ZIP_FILE}"
      DEST_DIR="${BASE_DIR}/${PREFIJO}_${YMM}"
      URL="https://contrataciondelsectorpublico.gob.es/sindicacion/sindicacion_${ID}/${NOMBRE}_${YMM}.zip"

      echo "🔽 Procesando $URL"

      # Descargar si no existe
      if [ ! -f "$DEST_ZIP" ]; then
        echo "  📥 Descargando..."
        if curl -sSf "$URL" -o "$DEST_ZIP"; then
          echo "  ✅ Guardado: $ZIP_FILE"
        else
          echo "  ❌ Error al descargar: $URL"
          rm -f "$DEST_ZIP"
          continue
        fi
      else
        echo "  ⏭️ Ya descargado"
      fi

      # Descomprimir si no existe
      if [ ! -d "$DEST_DIR" ]; then
        echo "  📦 Descomprimiendo en $DEST_DIR"
        mkdir -p "$DEST_DIR"
        if unzip -q "$DEST_ZIP" -d "$DEST_DIR"; then
          echo "  ✅ Descomprimido"
        else
          echo "  ❌ Error al descomprimir"
        fi
      else
        echo "  ⏭️ Ya descomprimido"
      fi

      echo ""
    done
  done
done
