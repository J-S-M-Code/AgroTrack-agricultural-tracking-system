package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ObservationField {
    @Getter
    @Setter
    private UUID idObservation;
    @Getter
    private Field field;
    @Getter
    private User user;
    @Getter
    private String description;
    @Getter
    private Polygon polygon;
    @Getter
    private List<String> urlPhoto;
    @Getter
    private LocalDateTime date;

    private ObservationField(Field field, User user, String description, Polygon polygon, List<String> urlPhoto, LocalDateTime date) {
        this.field = field;
        this.user = user;
        this.description = description;
        this.polygon = polygon;
        this.urlPhoto = urlPhoto;
        this.date = date;
    }

    public static ObservationField create(Field field, User user, String description, Polygon polygon, List<String> urlPhoto, LocalDateTime date){
        if (field == null){
            throw new BusinessRuleViolationsException("Debe estar asignado a un campo/lote existente");
        }
        if (user == null){
            throw new BusinessRuleViolationsException("Debe estar registrado para asignar una observacion");
        }
        if (polygon == null || polygon.isEmpty()){
            throw new BusinessRuleViolationsException("El perimetro de la finca no puede estar vacio");
        }
        if (!polygon.isValid()){
            throw new BusinessRuleViolationsException("La geometria del poligono es invalida");
        }
        if (urlPhoto == null || urlPhoto.isEmpty()){
            urlPhoto = new ArrayList<>();
        }
        if (date == null){
            throw new BusinessRuleViolationsException("Error al asignar la fecha de la observacion");
        }
        return new ObservationField(field, user, description, polygon, urlPhoto, date);
    }

    public void addPhoto(String newUrlPhoto){
        this.urlPhoto.add(newUrlPhoto);
    }
}
