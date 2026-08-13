package com.agrotrack.application.services;

import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.MapStatus;
import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
import com.agrotrack.domain.port.out.lot.MapTilingPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class AsyncMapProcessor {

    private final MapTilingPort mapTilingPort;
    private final SpectralMapRepositoryPort spectralMapRepositoryPort;
    private final SimpMessagingTemplate messagingTemplate;

    public AsyncMapProcessor(MapTilingPort mapTilingPort,
                             SpectralMapRepositoryPort spectralMapRepositoryPort,
                             SimpMessagingTemplate messagingTemplate) {
        this.mapTilingPort = mapTilingPort;
        this.spectralMapRepositoryPort = spectralMapRepositoryPort;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    public void processAndTileMap(SpectralMap map, String rawTifPath, SpectralMapType mapType) {
        System.out.println("🚀 [Async] Iniciando procesamiento de mosaico para mapa ID: " + map.getIdMap());
        
        try {
            // Cambiar estado a PROCESANDO
            updateMapState(map, MapStatus.PROCESSING, null);

            String flightDateStr = map.getFlightDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Invocar el adaptador que interactúa con MapProcessing.py
            mapTilingPort.processAndStoreTiles(rawTifPath, map.getIdMap(), map.getFarmId(), mapType, flightDateStr);
                
            // Formato estándar XYZ para capas de mapas en frontend web/móvil
            String tilesBaseUrl = "mapas-espectrales/procesados/" + map.getFarmId() + "/" + flightDateStr + "/" + map.getIdMap() + "/";
                
            // Cambiar estado final a LISTO y asociar su URL de teselas
            updateMapState(map, MapStatus.READY, tilesBaseUrl);
            System.out.println("✅ [Async] Procesamiento completado con éxito para mapa ID: " + map.getIdMap());
            
            // Notificar al Frontend por WebSockets
            notifyFrontend(map.getFarmId().toString(), map.getIdMap().toString(), "READY");

        } catch (Exception e) {
            System.err.println("❌ [Async] Error crítico en mapa ID " + map.getIdMap() + ": " + e.getMessage());
            updateMapState(map, MapStatus.ERROR, null);
            notifyFrontend(map.getFarmId().toString(), map.getIdMap().toString(), "ERROR");
        }
    }

    private void notifyFrontend(String farmId, String mapId, String status) {
        String topic = "/topic/farms/" + farmId + "/maps";
        String message = String.format("{\"mapId\":\"%s\", \"status\":\"%s\"}", mapId, status);
        messagingTemplate.convertAndSend(topic, message);
    }

    private void updateMapState(SpectralMap map, MapStatus status, String tilesUrl) {
        map.setMapStatus(status);
        if (tilesUrl != null) {
            map.setTilesBaseUrl(tilesUrl); // Asegúrate de tener este setter en tu entidad de dominio
        }
        spectralMapRepositoryPort.save(map);
    }
}