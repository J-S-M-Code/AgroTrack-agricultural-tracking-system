package com.agrotrack.domain.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Land;
import org.locationtech.jts.geom.Polygon;

public class LandPerimeterService {
    private final LandRepositoryPort landRepository;

    public landPerimeterService(LandRepositoryPort landRepository) {
        this.landRepository = landRepository;
    }

    public void updateLandPerimeter(Land land, Polygon newPerimeter) {
        boolean isOverLapping = landRepository.existsOverlappingLand(newPerimeter, land.getId());
        if (isOverLapping) {
            throw new BusinessRuleViolationsException("El poligono ingresado se superpone con una finca vecina existente.");
        }
        land.modifyuPoligonLimited(newPerimeter);
        landRepository.save(land);
    }
}
