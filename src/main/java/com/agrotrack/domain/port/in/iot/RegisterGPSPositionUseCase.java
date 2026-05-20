package com.agrotrack.domain.port.in.iot;

import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterGPSPositionUseCase {
    // Al registrar la posición, el servicio de aplicación deberá:
    // 1. Buscar el collar y a qué animal está asignado.
    // 2. Buscar el Lote donde se supone que está el animal (AnimalMovement activo).
    // 3. Evaluar matemáticamente si el Point está dentro del Polygon del lote (Geofencing).
    // 4. Asignar el boolean isOutOfBounds y guardar.
    void executeRegisterGPSPosition(UUID collarId, LocalDateTime timestamp, Point coordinate);
}