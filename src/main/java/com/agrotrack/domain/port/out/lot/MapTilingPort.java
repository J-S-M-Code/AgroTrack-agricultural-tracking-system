package com.agrotrack.domain.port.out.lot;

import com.agrotrack.domain.model.enums.SpectralMapType;

public interface MapTilingPort {

    /**
     * Toma una URL de un archivo GeoTIFF almacenado en MinIO, lo descarga,
     * lo procesa para generar teselas (tiles XYZ) y las almacena automáticamente
     * en el sistema de archivos (MinIO) aplicando el estilo correspondiente al tipo de mapa.
     *
     * @param tifUrl URL del archivo GeoTIFF en MinIO
     * @param mapId El ID del mapa
     * @param mapType El tipo de mapa (RGB, NDVI, GNDVI, etc.)
     */
    void processAndStoreTiles(String tifUrl, String mapId, SpectralMapType mapType);
}