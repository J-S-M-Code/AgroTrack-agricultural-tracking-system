package com.agrotrack.domain.port.out.lot;

import com.agrotrack.domain.model.entities.Lot;
import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotRepositoryPort {
    Lot save(Lot lot);
    Optional<Lot> findById(UUID id);
    List<Lot> findByFarmId(UUID farmId);
    List<Lot> findUnassignedLotsByUserId(UUID userId);
    boolean existsOverlappingLot(Polygon newPerimeter, UUID excludeLotId);
}