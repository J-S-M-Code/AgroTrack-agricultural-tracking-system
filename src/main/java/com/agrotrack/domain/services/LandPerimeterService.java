package com.agrotrack.domain.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Land;
import com.agrotrack.domain.port.out.land.LandRepositoryPort;
import org.locationtech.jts.geom.Polygon;

public class LandPerimeterService {
    private final LandRepositoryPort landRepository;

    public LandPerimeterService(LandRepositoryPort landRepository) {
        this.landRepository = landRepository;
    }

    public void updateLandPerimeter(Land land, Polygon newPerimeter) {
        boolean isOverLapping = landRepository.existsOverLappingLand(newPerimeter, land.getIdLand());
        if (isOverLapping) {
            throw new BusinessRuleViolationsException("El poligono ingresado se superpone con una finca vecina existente.");
        }
        land.modifyPolygonLimit(newPerimeter);
        landRepository.save(land);
    }
}
