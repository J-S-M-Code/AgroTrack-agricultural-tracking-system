package com.agrotrack.domain.port.out.land;

import com.agrotrack.domain.model.entities.Land;
import org.locationtech.jts.geom.Polygon;

import java.util.Optional;
import java.util.UUID;

public interface LandRepositoryPort {
    Land save(Land land);
    Optional<Land> findById(UUID id);
    boolean existsOverLappingLand(Polygon newPerimeter, UUID excludeLandId);
}
