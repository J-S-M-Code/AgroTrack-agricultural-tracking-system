package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.MapStatus;
import com.agrotrack.domain.model.enums.SpectralMapType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private LocalDateTime flightDate;
    private SpectralMapType indexType;
    private Double cloudCoverPercentage;
    private Double resolutionGSD;
    private Double meanIndexValue;
    private MapStatus mapStatus;
}
