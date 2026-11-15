package com.agrotrack.infrastructure.adapters.out.lot;

import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.out.lot.MapTilingPort;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Disabled;

@Disabled("Ignorado temporalmente porque el motor Python se queda colgado esperando a MinIO/GeoTIFF")
@SpringBootTest
class MapTilingAdapterTest {

    @Autowired
    private MapTilingPort mapTilingPort;

    @Test
    void shouldProcessAndUploadGeoTiffToStorageRGB() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y STORAGE ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/RGB.tif";
            SpectralMapType mapType = SpectralMapType.RGB;

            UUID fakeMapId = UUID.randomUUID();
            UUID farmId = UUID.randomUUID();
            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, farmId, mapType, "2024-01-01T12-00-00");

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }

    @Test
    void shouldProcessAndUploadGeoTiffToStorageNDVI() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y STORAGE ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/NDVI.tif";
            SpectralMapType mapType = SpectralMapType.NDVI;

            UUID fakeMapId = UUID.randomUUID();
            UUID farmId = UUID.randomUUID();
            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, farmId, mapType, "2024-01-01T12-00-00");

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }

    @Test
    void shouldProcessAndUploadGeoTiffToStorageGNDVI() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y STORAGE ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/GNDVI.tif";
            SpectralMapType mapType = SpectralMapType.GNDVI;

            UUID fakeMapId = UUID.randomUUID();
            UUID farmId = UUID.randomUUID();
            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, farmId, mapType, "2024-01-01T12-00-00");

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }

    @Test
    void shouldProcessAndUploadGeoTiffToStorageLCI() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y STORAGE ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/LCI.tif";
            SpectralMapType mapType = SpectralMapType.LCI;

            UUID fakeMapId = UUID.randomUUID();
            UUID farmId = UUID.randomUUID();
            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, farmId, mapType, "2024-01-01T12-00-00");

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }

    @Test
    void shouldProcessAndUploadGeoTiffToStorageNDRE() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y STORAGE ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/NDRE.tif";
            SpectralMapType mapType = SpectralMapType.NDRE;

            UUID fakeMapId = UUID.randomUUID();
            UUID farmId = UUID.randomUUID();
            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, farmId, mapType, "2024-01-01T12-00-00");

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }

    @Test
    void shouldProcessAndUploadGeoTiffToStorageOSAVI() {
        System.out.println("=== INICIANDO TEST DE INTEGRACIÓN DE GEOTOOLS Y STORAGE ===");

        try {
            String geoTiffURL = "http://localhost:9000/agrotrack-files/mapas-crudos/OSAVI.tif";
            SpectralMapType mapType = SpectralMapType.OSAVI;

            UUID fakeMapId = UUID.randomUUID();
            UUID farmId = UUID.randomUUID();
            mapTilingPort.processAndStoreTiles(geoTiffURL, fakeMapId, farmId, mapType, "2024-01-01T12-00-00");

            System.out.println("=== TEST FINALIZADO CON ÉXITO ===");
            System.out.println("¡Revisa tu consola de MinIO en http://localhost:9001!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("El test falló: " + e.getMessage());
        }
    }
}
