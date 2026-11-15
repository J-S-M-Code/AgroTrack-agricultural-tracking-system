package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.MapStatus;
import com.agrotrack.domain.model.enums.SpectralMapType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpectralMapDto {
    private UUID idMap;
    private String minioRawPath;
    private String tilesBaseUrl;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss][.SSS]") // Permite múltiples formatos ISO locales
    private LocalDateTime flightDate;
    private SpectralMapType indexType;
    private Double cloudCoverPercentage;
    private Double resolutionGSD;
    private Double meanIndexValue;
    private String description;
    private MapStatus mapStatus;
}
