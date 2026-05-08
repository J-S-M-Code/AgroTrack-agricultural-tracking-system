package com.agrotrack.domain.port.in.farm;

import org.locationtech.jts.geom.Polygon;
import java.util.UUID;

public interface UpdateFarmPerimeterUseCase {
    void execute(UUID farmId, Polygon newPerimeter);
}