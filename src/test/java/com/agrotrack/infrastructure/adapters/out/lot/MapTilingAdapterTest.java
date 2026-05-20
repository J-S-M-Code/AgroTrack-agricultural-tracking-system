package com.agrotrack.infrastructure.adapters.out.lot;

import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.out.lot.MapTilingPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MapTilingAdapterTest {

    @Autowired
    private MapTilingPort mapTilingPort;

    @Test
    void shouldProcessAndUploadGeoTiffToMinioRGB() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y MINIO ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/RGB.tif";
            String fakeMapId = "test-map-rgb-12345678";
            SpectralMapType mapType = SpectralMapType.RGB;

            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, mapType);

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }

    @Test
    void shouldProcessAndUploadGeoTiffToMinioNDVI() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y MINIO ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/NDVI.tif";
            String fakeMapId = "test-map-ndvi-12345678";
            SpectralMapType mapType = SpectralMapType.NDVI;

            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, mapType);

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }

    @Test
    void shouldProcessAndUploadGeoTiffToMinioGNDVI() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y MINIO ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/GNDVI.tif";
            String fakeMapId = "test-map-gndvi-12345678";
            SpectralMapType mapType = SpectralMapType.GNDVI;

            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, mapType);

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }

    @Test
    void shouldProcessAndUploadGeoTiffToMinioLCI() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y MINIO ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/LCI.tif";
            String fakeMapId = "test-map-lci-12345678";
            SpectralMapType mapType = SpectralMapType.LCI;

            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, mapType);

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }

    @Test
    void shouldProcessAndUploadGeoTiffToMinioNDRE() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y MINIO ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/NDRE.tif";
            String fakeMapId = "test-map-ndre-12345678";
            SpectralMapType mapType = SpectralMapType.NDRE;

            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, mapType);

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }

    @Test
    void shouldProcessAndUploadGeoTiffToMinioOSAVI() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y MINIO ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/OSAVI.tif";
            String fakeMapId = "test-map-osavi-12345678";
            SpectralMapType mapType = SpectralMapType.OSAVI;

            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, mapType);

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }
}