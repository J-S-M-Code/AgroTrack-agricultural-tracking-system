package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.AccionType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TaskDto {
    private UUID idTask;
    private String title;
    private AccionType accionType;
    private TaskStatus taskStatus;
    private LocalDateTime dueDate;
    private Priority priority;
    private LocalDateTime creationDate;
    private LocalDateTime completeDate;
    private String description;
    private Polygon polygonLimit;
    private Point centroid;
    private List<String> images;
    
    // IDs de relaciones en lugar de las entidades completas
    private UUID creatorId;
    private UUID assignedId;
    private UUID relatedFarmId;
    private UUID relatedLotId;
}
