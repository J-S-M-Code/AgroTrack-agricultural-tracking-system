package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.TypeSpectralMap;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpectralMap {
    @Getter
    @Setter
    private UUID idMap;
    @Getter
    private Field field;
    @Getter
    private String urlSpectralMap;
    @Getter
    private LocalDateTime flightDate;
    @Getter
    private TypeSpectralMap indexType;

    private SpectralMap(Field field, String urlSpectralMap, LocalDateTime flightDate, TypeSpectralMap indexType) {
        this.field = field;
        this.urlSpectralMap = urlSpectralMap;
        this.flightDate = flightDate;
        this.indexType = indexType;
    }

    public static SpectralMap create(Field field, String urlSpectralMap, LocalDateTime flightDate, TypeSpectralMap indexType) {
        if (field == null) {
            throw new BusinessRuleViolationsException("Debe estar asignado a un campo/lote existente");
        }
        if (urlSpectralMap == null || urlSpectralMap.isBlank()) {
            throw new BusinessRuleViolationsException("Error al asignar la URL del mapa");
        }
        if (flightDate == null) {
            throw new BusinessRuleViolationsException("Debe seleccionar una fecha de vuelo");
        }
        if (indexType == null) {
            throw new BusinessRuleViolationsException("El campo Tipo de Mapa  no puede estar vacio");
        }
        return new SpectralMap(field, urlSpectralMap, flightDate, indexType);
    }
}
