package com.agrotrack.domain.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Field;
import com.agrotrack.domain.model.entities.Land;
import com.agrotrack.domain.port.out.field.FieldRepositoryPort;
import org.locationtech.jts.geom.Polygon;

public class FieldPerimeterService {
    private final FieldRepositoryPort fieldRepository;

    public FieldPerimeterService(FieldRepositoryPort fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    public void updateLandPerimeter(Field field, Polygon newPerimeter) {
        boolean isOverLapping = fieldRepository.existsOverLappingField(newPerimeter, field.getIdField());
        if (isOverLapping) {
            throw new BusinessRuleViolationsException("El poligono ingresado se superpone con una finca vecina existente.");
        }
        field.modifyPolygonLimit(newPerimeter);
        fieldRepository.save(field);
    }
}
