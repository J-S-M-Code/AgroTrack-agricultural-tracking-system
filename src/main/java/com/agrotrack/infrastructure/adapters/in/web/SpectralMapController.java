package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.SpectralMapDto;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.port.in.lot.RegisterSpectralMapUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lots/{lotId}/maps")
public class SpectralMapController {

    private final RegisterSpectralMapUseCase registerSpectralMapUseCase;

    public SpectralMapController(RegisterSpectralMapUseCase registerSpectralMapUseCase) {
        this.registerSpectralMapUseCase = registerSpectralMapUseCase;
    }

    @PostMapping
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')") // APPLICATOR is normally worker level, AGRONOMIST or OWNER register maps
    public ResponseEntity<SpectralMapDto> registerMap(@PathVariable UUID lotId, @RequestBody SpectralMapDto request, @RequestParam UUID farmId) {
        // El frontend subió el archivo a MinIO y nos envía los metadatos y la ruta cruda (minioRawPath)
        SpectralMap savedMap = registerSpectralMapUseCase.executeRegisterSpectralMap(
                request.getMinioRawPath(),
                request.getFlightDate(),
                request.getIndexType(),
                request.getCloudCoverPercentage(),
                request.getResolutionGSD(),
                request.getMeanIndexValue(),
                lotId
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
                .mapStatus(savedMap.getMapStatus())
                .build();

        return ResponseEntity.ok(response);
    }
}
