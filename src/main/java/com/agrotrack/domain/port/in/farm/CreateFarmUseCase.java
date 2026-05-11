package com.agrotrack.domain.port.in.farm;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.enums.ProductiveOrientation;
import org.locationtech.jts.geom.Polygon;

public interface CreateFarmUseCase {
    Farm execute(String name, String companyName, String cuit, String numberRENAPSA,
                 ProductiveOrientation productiveOrientation, String address,
                 Polygon polygonLimit, double surface, String imageUrl);
}