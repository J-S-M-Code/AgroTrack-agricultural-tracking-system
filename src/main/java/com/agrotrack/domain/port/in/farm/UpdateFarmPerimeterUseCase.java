package com.agrotrack.domain.port.in.farm;

import org.locationtech.jts.geom.Polygon;
import java.util.UUID;

public interface UpdateFarmPerimeterUseCase {
    void executeUpdateFarmPerimeter(UUID farmId, Polygon newPerimeter);
}