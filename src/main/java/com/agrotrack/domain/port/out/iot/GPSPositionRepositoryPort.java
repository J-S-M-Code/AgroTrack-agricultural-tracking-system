package com.agrotrack.domain.port.out.iot;

import com.agrotrack.domain.model.entities.GPSPosition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GPSPositionRepositoryPort {
    GPSPosition save(GPSPosition gpsPosition);

    // Para mostrar el recorrido del animal en el mapa en un día/semana particular
    List<GPSPosition> findByCollarIdAndDateRange(UUID collarId, LocalDateTime start, LocalDateTime end);

    // Para ver exactamente dónde está el animal ahora mismo
    Optional<GPSPosition> findLatestByCollarId(UUID collarId);
}