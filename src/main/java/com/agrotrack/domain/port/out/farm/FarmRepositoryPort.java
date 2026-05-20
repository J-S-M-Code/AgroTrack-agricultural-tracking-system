package com.agrotrack.domain.port.out.farm;

import com.agrotrack.domain.model.entities.Farm;
import org.locationtech.jts.geom.Polygon;

import java.util.Optional;
import java.util.UUID;


public interface FarmRepositoryPort {
    Farm save(Farm farm);
    Optional<Farm> findById(UUID id);
    boolean existsOverlappingFarm(Polygon newPerimeter, UUID excludeFarmId);
    boolean existsByCuit(String cuit);
}