package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.AccionType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Task {
    @Getter
    @Setter
    private UUID idTask;
    @Getter
    private String title;
    @Getter
    private AccionType accionType;
    @Getter
    private TaskStatus taskStatus;
    @Getter
    private LocalDateTime dueDate;
    @Getter
    private Priority priority;
    @Getter
    private LocalDateTime creationDate;
    @Getter
    private LocalDateTime completeDate;
    @Getter
    private User creator;
    @Getter
    private User assigned;
    @Getter
    private Farm relatedFarm;
    @Getter
    private Lot relatedLot;
    @Getter
    private Polygon polygonLimit;
    @Getter
    private Point centroid;
    @Getter
    private List<String> images;
    @Getter
    private String description;

    private Task(String title, AccionType accionType, TaskStatus taskStatus, LocalDateTime dueDate, Priority priority,
                 LocalDateTime creationDate, LocalDateTime completeDate, User creator, User assigned, Farm relatedFarm,
                 Lot relatedLot, Polygon polygonLimit, Point centroid, List<String> images, String description) {
        this.title = title;
        this.accionType = accionType;
        this.taskStatus = taskStatus;
        this.dueDate = dueDate;
        this.priority = priority;
        this.creationDate = creationDate;
        this.completeDate = completeDate;
        this.creator = creator;
        this.assigned = assigned;
        this.relatedFarm = relatedFarm;
        this.relatedLot = relatedLot;
        this.polygonLimit = polygonLimit;
        this.centroid = centroid;
        this.images = images;
        this.description = description;
    }

    public static Task create(String title, AccionType accionType, TaskStatus taskStatus, LocalDateTime dueDate,
                              Priority priority, LocalDateTime creationDate, LocalDateTime completeDate, User creator,
                              User assigned, Farm relatedFarm, Lot relatedLot, Polygon polygonLimit, Point centroid,
                              List<String> images, String description) {

        if (title == null || title.isBlank()) {
            throw new BusinessRuleViolationsException("El título no puede ser nulo o estar vacío");
        }

        if (accionType == null) {
            throw new BusinessRuleViolationsException("El tipo de acción no puede ser nulo");
        }
        if (taskStatus == null) {
            throw new BusinessRuleViolationsException("El estado de la tarea no puede ser nulo");
        }
        if (priority == null) {
            throw new BusinessRuleViolationsException("La prioridad no puede ser nula");
        }

        if (creator == null) {
            throw new BusinessRuleViolationsException("El creador no puede ser nulo");
        }
        if (assigned == null) {
            throw new BusinessRuleViolationsException("Debe haber un usuario asignado a la tarea");
        }

        if (relatedFarm == null) {
            throw new BusinessRuleViolationsException("La finca debe estar seleccionada obligatoriamente");
        }

        if (creationDate == null) {
            throw new BusinessRuleViolationsException("La fecha de creación no puede ser nula");
        }
        if (creationDate.toLocalDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleViolationsException("La fecha de creación no puede ser mayor a la fecha actual");
        }

        if (dueDate == null) {
            throw new BusinessRuleViolationsException("La fecha de vencimiento (fecha mínima) no puede ser nula");
        }
        if (dueDate.toLocalDate().isBefore(creationDate.toLocalDate())) {
            throw new BusinessRuleViolationsException("La fecha mínima de la tarea no puede ser anterior a su fecha de creación");
        }

        // Lógica de polígono y centroide similar a Alert
        if (relatedFarm != null || relatedLot != null) {
            if (polygonLimit == null) {
                if (relatedLot == null) {
                    polygonLimit = relatedFarm.getPolygonLimit();
                    centroid = relatedFarm.getPolygonLimit().getCentroid();
                } else {
                    polygonLimit = relatedLot.getPolygonLimit();
                    centroid = relatedLot.getPolygonLimit().getCentroid();
                }
            }
        }

        return new Task(title, accionType, taskStatus, dueDate, priority, creationDate, completeDate, creator, assigned, relatedFarm, relatedLot, polygonLimit, centroid, images, description);
    }
}