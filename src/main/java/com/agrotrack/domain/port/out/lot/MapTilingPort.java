package com.agrotrack.domain.port.out.lot;

import java.util.UUID;

import com.agrotrack.domain.model.enums.SpectralMapType;

public interface MapTilingPort {

    /**
     * Toma una URL de un archivo GeoTIFF almacenado en MinIO, lo descarga,
     * lo procesa para generar teselas (tiles XYZ) y las almacena automáticamente
     * en el sistema de archivos (MinIO) aplicando el estilo correspondiente al tipo de mapa.
     *
     * @param tifUrl URL del archivo GeoTIFF en MinIO
     * @param mapId El ID del mapa
     * @param idFamr El ID de la finca
     * @param idLot El ID de la lote
     * @param mapType El tipo de mapa (RGB, NDVI, GNDVI, etc.)
     */
    void processAndStoreTiles(String tifUrl, UUID mapId, UUID idFamr, UUID idLot, SpectralMapType mapType);
}