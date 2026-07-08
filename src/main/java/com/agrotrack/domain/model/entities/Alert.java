package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.AlertType;
import com.agrotrack.domain.model.enums.RecordType;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Alert {
    @Getter
    @Setter
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private UUID idAlert;
    @Getter
    private String title;
    @Getter
    private AlertType alertType;
    @Getter
    private Priority priority;
    @Getter
    private RecordType recordType ;
    @Getter
    private String description;
    @Getter
    private LocalDateTime createdAt;
    @Getter
    private User author;
    @Getter
    private List<String> images;
    @Getter
    private Lot relatedLot;
    @Getter
    private Crop relatedCrop;
    @Getter
    private Animal relatedAnimal;
    @Getter
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Polygon polygonLimit;
    @Getter
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Point centroid;


    private Alert (String title, AlertType alertType, Priority priority, RecordType recordType, String description,
                   LocalDateTime createdAt, User autor, List<String> images, Lot relatedLot, Crop relatedCrop,
                   Animal relatedAnimal,Polygon polygonLimit, Point centroid){
        this.title = title;
        this.alertType = alertType;
        this.priority = priority;
        this.recordType = recordType;
        this.description = description;
        this.createdAt = createdAt;
        this.author = autor;
        this.images = images;
        this.relatedLot = relatedLot;
        this.relatedCrop = relatedCrop;
        this.relatedAnimal = relatedAnimal;
        this.polygonLimit = polygonLimit;
        this.centroid = centroid;
    }

    public static Alert create (String title, AlertType alertType, Priority priority, RecordType recordType,
                                String description, LocalDateTime createdAt, User autor, List<String> images, Lot relatedLot,
                                Crop relatedCrop, Animal relatedAnimal,Polygon polygonLimit, Point centroid){
        if (title == null || title.isBlank()){
            throw new BusinessRuleViolationsException("El titulo no puede ser nulo");
        }
        if (alertType == null){
            throw new BusinessRuleViolationsException("El tipo de alerta no puede ser nulo");
        }
        if (priority == null){
           throw new BusinessRuleViolationsException("La prioridad no puede ser nula");
        }
        if (recordType == null){
            throw new BusinessRuleViolationsException("El tipo de registro no puede ser nulo");
        }
        if (createdAt == null){
            throw new BusinessRuleViolationsException("La fecha no puede ser nula");
        }
        if (autor == null){
            throw new BusinessRuleViolationsException("Debe tener un creador");
        }
        if (relatedLot == null && relatedCrop == null && relatedAnimal == null){
            throw new BusinessRuleViolationsException("La alerta debe estar asignada por lo menos a un animal, lote o cultivo");
        }
        if (relatedLot != null || relatedCrop != null){
            if (polygonLimit == null){
                if (relatedLot == null){
                    polygonLimit = relatedCrop.getAssignedLot().getPolygonLimit();
                    centroid = relatedCrop.getAssignedLot().getPolygonLimit().getCentroid();
                } else {
                    polygonLimit = relatedLot.getPolygonLimit();
                    centroid = relatedLot.getPolygonLimit().getCentroid();
                }
            }
        }

        return new Alert(title, alertType, priority, recordType, description, createdAt, autor, images, relatedLot, relatedCrop, relatedAnimal, polygonLimit, centroid);
    }
}
