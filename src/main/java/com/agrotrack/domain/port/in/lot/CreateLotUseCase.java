package com.agrotrack.domain.port.in.lot;

import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.SoilType;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;

public interface CreateLotUseCase {
    Lot executeCreateLot(UUID farmId, String name, double hectares, SoilType soilType,
                LotType type, String description, Polygon polygonLimit);
}