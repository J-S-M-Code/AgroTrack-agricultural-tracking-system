package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.SpectralMapDto;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.port.in.lot.DeleteSpectralMapUseCase;
import com.agrotrack.domain.port.in.lot.GeneratePresignedUrlUseCase;
import com.agrotrack.domain.port.in.lot.GetSpectralMapsUseCase;
import com.agrotrack.domain.port.in.lot.RegisterSpectralMapUseCase;
import com.agrotrack.domain.port.in.lot.UpdateSpectralMapUseCase;
import com.agrotrack.domain.port.in.lot.RetrySpectralMapUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/farms/{farmId}/maps")
public class SpectralMapController {

    private final RegisterSpectralMapUseCase registerSpectralMapUseCase;
    private final GetSpectralMapsUseCase getSpectralMapsUseCase;
    private final DeleteSpectralMapUseCase deleteSpectralMapUseCase;
    private final GeneratePresignedUrlUseCase generatePresignedUrlUseCase;
    private final UpdateSpectralMapUseCase updateSpectralMapUseCase;
    private final RetrySpectralMapUseCase retrySpectralMapUseCase;

    public SpectralMapController(RegisterSpectralMapUseCase registerSpectralMapUseCase,
                                 GetSpectralMapsUseCase getSpectralMapsUseCase,
                                 DeleteSpectralMapUseCase deleteSpectralMapUseCase,
                                 GeneratePresignedUrlUseCase generatePresignedUrlUseCase,
                                 UpdateSpectralMapUseCase updateSpectralMapUseCase,
                                 RetrySpectralMapUseCase retrySpectralMapUseCase) {
        this.registerSpectralMapUseCase = registerSpectralMapUseCase;
        this.getSpectralMapsUseCase = getSpectralMapsUseCase;
        this.deleteSpectralMapUseCase = deleteSpectralMapUseCase;
        this.generatePresignedUrlUseCase = generatePresignedUrlUseCase;
        this.updateSpectralMapUseCase = updateSpectralMapUseCase;
        this.retrySpectralMapUseCase = retrySpectralMapUseCase;
    }

    @PostMapping
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')") // APPLICATOR is normally worker level, AGRONOMIST or OWNER register maps
    public ResponseEntity<SpectralMapDto> registerMap(@PathVariable UUID farmId, @RequestBody SpectralMapDto request) {
        // El frontend subió el archivo a MinIO y nos envía los metadatos y la ruta cruda (minioRawPath)
        SpectralMap savedMap = registerSpectralMapUseCase.executeRegisterSpectralMap(
                request.getMinioRawPath(),
                request.getFlightDate(),
                request.getIndexType(),
                request.getCloudCoverPercentage(),
                request.getResolutionGSD(),
                request.getMeanIndexValue(),
                farmId,
                request.getDescription()
        );

        SpectralMapDto response = SpectralMapDto.builder()
                .idMap(savedMap.getIdMap())
                .minioRawPath(savedMap.getUrlSpectralMap())
                .tilesBaseUrl(savedMap.getTilesBaseUrl())
                .flightDate(savedMap.getFlightDate())
                .indexType(savedMap.getIndexType())
                .cloudCoverPercentage(savedMap.getCloudCoverPercentage())
                .resolutionGSD(savedMap.getResolutionGSD())
                .meanIndexValue(savedMap.getMeanIndexValue())
                .description(savedMap.getDescription())
                .mapStatus(savedMap.getMapStatus())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasPermission(#farmId, 'ANY')") // Cualquiera puede ver los mapas si tiene acceso a la finca
    public ResponseEntity<List<SpectralMapDto>> getMapsByFarm(@PathVariable UUID farmId) {
        List<SpectralMap> maps = getSpectralMapsUseCase.executeGetSpectralMapsByFarm(farmId);
        List<SpectralMapDto> response = maps.stream().map(map -> SpectralMapDto.builder()
                .idMap(map.getIdMap())
                .minioRawPath(map.getUrlSpectralMap())
                .tilesBaseUrl(map.getTilesBaseUrl())
                .flightDate(map.getFlightDate())
                .indexType(map.getIndexType())
                .cloudCoverPercentage(map.getCloudCoverPercentage())
                .resolutionGSD(map.getResolutionGSD())
                .meanIndexValue(map.getMeanIndexValue())
                .description(map.getDescription())
                .mapStatus(map.getMapStatus())
                .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{mapId}")
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')")
    public ResponseEntity<Void> deleteMap(@PathVariable UUID farmId, @PathVariable UUID mapId) {
        System.out.println(">>> RECIBIDA PETICION DELETE PARA MAPA: " + mapId + " EN FINCA: " + farmId);
        deleteSpectralMapUseCase.executeDeleteSpectralMap(mapId);
        System.out.println(">>> MAPA ELIMINADO CORRECTAMENTE: " + mapId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{mapId}")
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')")
    public ResponseEntity<SpectralMapDto> updateMap(@PathVariable UUID farmId, @PathVariable UUID mapId, @RequestBody SpectralMapDto request) {
        SpectralMap updatedMap = updateSpectralMapUseCase.executeUpdateSpectralMap(
                mapId,
                request.getCloudCoverPercentage(),
                request.getResolutionGSD(),
                request.getMeanIndexValue(),
                request.getDescription()
        );
        
        SpectralMapDto response = SpectralMapDto.builder()
                .idMap(updatedMap.getIdMap())
                .minioRawPath(updatedMap.getUrlSpectralMap())
                .tilesBaseUrl(updatedMap.getTilesBaseUrl())
                .flightDate(updatedMap.getFlightDate())
                .indexType(updatedMap.getIndexType())
                .cloudCoverPercentage(updatedMap.getCloudCoverPercentage())
                .resolutionGSD(updatedMap.getResolutionGSD())
                .meanIndexValue(updatedMap.getMeanIndexValue())
                .description(updatedMap.getDescription())
                .mapStatus(updatedMap.getMapStatus())
                .build();
                
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{mapId}/retry")
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')")
    public ResponseEntity<SpectralMapDto> retryProcessing(@PathVariable UUID farmId, @PathVariable UUID mapId) {
        SpectralMap updatedMap = retrySpectralMapUseCase.executeRetryProcessing(mapId);
        
        SpectralMapDto response = SpectralMapDto.builder()
                .idMap(updatedMap.getIdMap())
                .minioRawPath(updatedMap.getUrlSpectralMap())
                .tilesBaseUrl(updatedMap.getTilesBaseUrl())
                .flightDate(updatedMap.getFlightDate())
                .indexType(updatedMap.getIndexType())
                .cloudCoverPercentage(updatedMap.getCloudCoverPercentage())
                .resolutionGSD(updatedMap.getResolutionGSD())
                .meanIndexValue(updatedMap.getMeanIndexValue())
                .description(updatedMap.getDescription())
                .mapStatus(updatedMap.getMapStatus())
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/presigned-url")
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@PathVariable UUID farmId, @RequestParam String fileName) {
        String url = generatePresignedUrlUseCase.executeGeneratePresignedUrl(farmId, fileName);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
