package com.agrotrack.domain.port.out.lot;

import com.agrotrack.domain.model.enums.SpectralMapType;
import java.io.InputStream;

public interface MapTilingPort {

    /**
     * Toma un archivo GeoTIFF original, lo procesa para generar teselas (tiles XYZ)
     * y las almacena automáticamente en el sistema de archivos (MinIO) aplicando
     * el estilo correspondiente al tipo de mapa.
     *
     * @param geoTiffStream El archivo original subido por el ingeniero
     * @param mapId El ID del mapa
     * @param mapType El tipo de mapa (RGB, NDVI, GNDVI, etc.)
     */
    void processAndStoreTiles(InputStream geoTiffStream, String mapId, SpectralMapType mapType);
}