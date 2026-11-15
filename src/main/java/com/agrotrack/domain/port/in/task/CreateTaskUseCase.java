package com.agrotrack.domain.port.in.task;

import com.agrotrack.domain.model.entities.Task;
import com.agrotrack.domain.model.enums.AccionType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.TaskStatus;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CreateTaskUseCase {
    Task executeCreateTask(String title, AccionType accionType, TaskStatus taskStatus, LocalDateTime dueDate,
                 Priority priority, LocalDateTime creationDate, LocalDateTime completeDate,
                 UUID creatorId, UUID assignedId, UUID relatedFarmId, UUID relatedLotId,
                 Polygon polygonLimit, Point centroid, List<String> images, String description);
}