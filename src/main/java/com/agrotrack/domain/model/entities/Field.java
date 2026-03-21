package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.TypeField;
import com.agrotrack.domain.model.enums.TypeFloor;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;


public class Field {
    @Getter
    @Setter
    private UUID idField;
    @Getter
    private Land land;
    @Getter
    private String name;
    @Getter
    @Setter
    private TypeFloor typeFloor;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private double surface;
    @Getter
    private Polygon polygonLimit;
    @Getter
    @Setter
    private String state;
    @Getter
    @Setter
    private TypeField type;

    private Field (Land land, String name, TypeFloor typeFloor, String description, Polygon polygonLimit, String state, TypeField type) {
        this.land = land;
        this.name = name;
        this.typeFloor = typeFloor;
        this.description = description;
        this.polygonLimit = polygonLimit;
        this.state = state;
        this.type = type;
    }

    public static Field create(Land land, String name, TypeFloor typeFloor, String description, Polygon polygonLimit, String state, TypeField type) {
        if (land == null) {
            throw new BusinessRuleViolationsException("Debe estar asignada a una finca existente");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Nombre no puede estar vacio");
        }
        if  (typeFloor == null) {
            throw new BusinessRuleViolationsException("El tipo de suelo no puede estar vacio");
        }
        if (polygonLimit == null || polygonLimit.isEmpty()){
            throw new BusinessRuleViolationsException("El perimetro de la finca no puede estar vacio");
        }
        if (!polygonLimit.isValid()){
            throw new BusinessRuleViolationsException("La geometria del poligono es invalida");
        }
        if (state == null || state.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Estado no puede estar vacio");
        }
        if (type == null) {
            throw new BusinessRuleViolationsException("El tipo de la finca no puede estar vacio");
        }
        return new Field(land, name, typeFloor, description, polygonLimit, state, type);
    }

    public void modifyuPoligonLimited(Polygon newPolygonLimit) {
        if (newPolygonLimit == null || newPolygonLimit.isEmpty()){
            throw new BusinessRuleViolationsException("El perimetro de la finca no puede estar vacio");
        }
        if (!newPolygonLimit.isValid()){
            throw new BusinessRuleViolationsException("La geometria del poligono es invalida");
        }
        this.polygonLimit = newPolygonLimit;
        this.surface = 0.0;
    }

}
