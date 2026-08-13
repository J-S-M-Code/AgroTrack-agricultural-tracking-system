package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.model.enums.MapStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpectralMap {
    @Getter
    @Setter
    private UUID idMap;

    @Getter
    @Setter
    private String urlSpectralMap;

    @Getter
    @Setter
    private String tilesBaseUrl;


    @Getter
    private LocalDateTime flightDate;

    @Getter
    private SpectralMapType indexType;

    @Getter
    @Setter
    private Double cloudCoverPercentage;

    @Getter
    @Setter
    private Double resolutionGSD; // Resolución en cm/píxel

    @Getter
    @Setter
    private Double meanIndexValue; // Valor medio del índice (ej. 0.65)

    @Getter
    private UUID farmId;

    @Getter
    @Setter
    private MapStatus mapStatus;

    @Getter
    @Setter
    private String description;

    private SpectralMap(String urlSpectralMap, LocalDateTime flightDate, SpectralMapType indexType,
                        Double cloudCoverPercentage, Double resolutionGSD, Double meanIndexValue, UUID farmId, String description) {
        this.urlSpectralMap = urlSpectralMap;
        this.flightDate = flightDate;
        this.indexType = indexType;
        this.cloudCoverPercentage = cloudCoverPercentage;
        this.resolutionGSD = resolutionGSD;
        this.meanIndexValue = meanIndexValue;
        this.farmId = farmId;
        this.description = description;
        this.mapStatus = MapStatus.PENDING;
    }

    public static SpectralMap create(String urlSpectralMap, LocalDateTime flightDate, SpectralMapType indexType,
                                     Double cloudCoverPercentage, Double resolutionGSD, Double meanIndexValue, UUID farmId, String description) {

        if (farmId == null) {
            throw new BusinessRuleViolationsException("El mapa debe estar asociado a una finca");
        }
        if (flightDate == null || flightDate.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleViolationsException("La fecha del vuelo no es válida");
        }
        if (indexType == null) {
            throw new BusinessRuleViolationsException("Debe especificar el tipo de índice espectral");
        }

        // Validación de porcentaje (0 a 100)
        if (cloudCoverPercentage == null || cloudCoverPercentage < 0 || cloudCoverPercentage > 100) {
            throw new BusinessRuleViolationsException("El porcentaje de nubosidad debe estar entre 0 y 100");
        }

        if (resolutionGSD != null && resolutionGSD <= 0) {
            throw new BusinessRuleViolationsException("La resolución GSD debe ser un valor positivo");
        }

        return new SpectralMap(urlSpectralMap, flightDate, indexType, cloudCoverPercentage, resolutionGSD, meanIndexValue, farmId, description);
    }

    // --- MÉTODOS DE COMPORTAMIENTO ---

    /**
     * Determina si el mapa es confiable basado en la nubosidad.
     * Generalmente, más del 20-30% de nubes invalida los datos espectrales.
     */
    public boolean isReliable() {
        return this.cloudCoverPercentage < 25.0;
    }

    /**
     * Compara el valor medio del índice con un umbral para determinar si el lote
     * requiere atención (ej. estrés hídrico o falta de nitrógeno).
     */
    public boolean requiresAttention(Double threshold) {
        if (this.meanIndexValue == null) return false;
        return this.meanIndexValue < threshold;
    }

    /**
     * Verifica si el mapa fue generado recientemente (en los últimos 7 días).
     */
    public boolean isRecent() {
        return this.flightDate.isAfter(LocalDateTime.now().minusDays(7));
    }
}