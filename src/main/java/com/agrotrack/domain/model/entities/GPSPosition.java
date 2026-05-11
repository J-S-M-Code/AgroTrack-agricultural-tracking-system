package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

public class GPSPosition {
    @Getter
    @Setter
    private UUID idGPSPosition;

    @Getter
    private LocalDateTime timestamp;

    @Getter
    private Point coordinate;

    @Getter
    private boolean isOutOfBounds;

    private GPSPosition(LocalDateTime timestamp, Point coordinate, boolean isOutOfBounds) {
        this.timestamp = timestamp;
        this.coordinate = coordinate;
        this.isOutOfBounds = isOutOfBounds;
    }

    public static GPSPosition create(LocalDateTime timestamp, Point coordinate, Boolean isOutOfBounds) {

        // Validación de Fecha/Hora
        if (timestamp == null) {
            throw new BusinessRuleViolationsException("El timestamp de la posición GPS no puede ser nulo");
        }
        if (timestamp.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleViolationsException("El registro GPS no puede tener una fecha/hora en el futuro");
        }

        // Validación de la Coordenada (Geometría JTS)
        if (coordinate == null || coordinate.isEmpty()) {
            throw new BusinessRuleViolationsException("La coordenada GPS no puede ser nula ni estar vacía");
        }
        if (!coordinate.isValid()) {
            throw new BusinessRuleViolationsException("La geometría de la coordenada GPS es inválida");
        }

        // Validación del estado de los límites
        if (isOutOfBounds == null) {
            throw new BusinessRuleViolationsException("Debe especificarse si la posición se encuentra fuera de los límites o no");
        }

        return new GPSPosition(timestamp, coordinate, isOutOfBounds);
    }

    // --- MÉTODOS DE COMPORTAMIENTO ---

    /**
     * Marca la posición GPS como una brecha de los límites del lote (Geofencing).
     * Útil si el sistema de validación de polígonos corre después de guardar el punto.
     */
    public void markAsOutOfBounds() {
        this.isOutOfBounds = true;
    }

    /**
     * Revierte o marca la posición como dentro de los límites permitidos.
     */
    public void markAsWithinBounds() {
        this.isOutOfBounds = false;
    }
}