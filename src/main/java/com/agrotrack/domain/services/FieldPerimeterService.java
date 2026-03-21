package com.agrotrack.domain.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Land;
import org.locationtech.jts.geom.Polygon;

public class FieldPerimeterService {
    private final FieldRepositoryPort fieldRepository;

    public fieldPerimeterService(FieldRepositoryPort fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    public void updateLandPerimeter(Land field, Polygon newPerimeter) {
        boolean isOverLapping = fieldRepository.existsOverlappingLand(newPerimeter, field.getId());
        if (isOverLapping) {
            throw new BusinessRuleViolationsException("El poligono ingresado se superpone con una finca vecina existente.");
        }
        field.modifyuPoligonLimited(newPerimeter);
        fieldRepository.save(field);
    }
}
