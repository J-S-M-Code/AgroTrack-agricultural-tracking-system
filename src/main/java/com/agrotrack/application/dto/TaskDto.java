package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.AccionType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private UUID idTask;
    private String title;
    private AccionType accionType;
    private TaskStatus taskStatus;
    private LocalDateTime dueDate;
    private Priority priority;
    private LocalDateTime creationDate;
    private LocalDateTime completeDate;
    private String description;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Polygon polygonLimit;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Point centroid;
    private List<String> images;
    
    // IDs de relaciones en lugar de las entidades completas
    private UUID creatorId;
    private String creatorName;
    private UUID assignedId;
    private String assignedName;
    private UUID relatedFarmId;
    private String farmName;
    private UUID relatedLotId;
    private String lotName;
}
