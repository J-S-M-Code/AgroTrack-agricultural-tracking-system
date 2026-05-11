package com.agrotrack.domain.port.in.alert;

import com.agrotrack.domain.model.entities.Alert;
import com.agrotrack.domain.model.enums.AlertType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.RecordType;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CreateAlertUseCase {
    Alert execute(String title, AlertType alertType, Priority priority, RecordType recordType,
                  String description, LocalDateTime createdAt, UUID authorId, List<String> images,
                  UUID relatedLotId, UUID relatedCropId, UUID relatedAnimalId,
                  Polygon polygonLimit, Point centroid);
}