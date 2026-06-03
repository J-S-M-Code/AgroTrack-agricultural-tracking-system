package com.agrotrack.application.services;

import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.MapStatus;
import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
import com.agrotrack.domain.port.out.lot.MapTilingPort;
import com.agrotrack.domain.port.out.storage.FileStoragePort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncMapProcessor {

    private final MapTilingPort mapTilingPort;
    private final SpectralMapRepositoryPort spectralMapRepositoryPort;

    public AsyncMapProcessor(MapTilingPort mapTilingPort,
                             FileStoragePort fileStoragePort,
                             SpectralMapRepositoryPort spectralMapRepositoryPort) {
        this.mapTilingPort = mapTilingPort;
        this.spectralMapRepositoryPort = spectralMapRepositoryPort;
    }

    @Async
    public void processAndTileMap(SpectralMap map, String rawTifPath, SpectralMapType mapType) {
        System.out.println("🚀 [Async] Iniciando procesamiento de mosaico para mapa ID: " + map.getIdMap());
        
        try {
            // Cambiar estado a PROCESANDO
            updateMapState(map, MapStatus.PROCESSING, null);

            // Invocar el adaptador que interactúa con MapProcessing.py
            mapTilingPort.processAndStoreTiles(rawTifPath, map.getIdMap(), map.getAssignedLot().getFarm().getIdFarm(), map.getAssignedLot().getIdLot(), mapType);
                
            // Formato estándar XYZ para capas de mapas en frontend web/móvil
            String tilesBaseUrl = "mapas-espectrales/" + map.getAssignedLot().getFarm().getIdFarm() + "/" + map.getAssignedLot().getIdLot() + "/" + mapType.toString() + "/";
                
            // Cambiar estado final a LISTO y asociar su URL de teselas
            updateMapState(map, MapStatus.READY, tilesBaseUrl);
            System.out.println("✅ [Async] Procesamiento completado con éxito para mapa ID: " + map.getIdMap());

        } catch (Exception e) {
            System.err.println("❌ [Async] Error crítico en mapa ID " + map.getIdMap() + ": " + e.getMessage());
            updateMapState(map, MapStatus.ERROR, null);
        }
    }

    private void updateMapState(SpectralMap map, MapStatus status, String tilesUrl) {
        map.setMapStatus(status);
        if (tilesUrl != null) {
            map.setTilesBaseUrl(tilesUrl); // Asegúrate de tener este setter en tu entidad de dominio
        }
        spectralMapRepositoryPort.save(map);
    }
}