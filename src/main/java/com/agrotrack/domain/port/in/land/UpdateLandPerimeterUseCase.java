package com.agrotrack.domain.port.in.land;

import org.locationtech.jts.geom.Polygon;

import java.util.UUID;

public interface UpdateLandPerimeterUseCase {
    void execute(UUID landId, Polygon newPerimeter);
}
