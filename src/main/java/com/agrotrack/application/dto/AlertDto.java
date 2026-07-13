package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.AlertType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.RecordType;
import com.agrotrack.application.dto.PointDto;
import com.agrotrack.application.dto.PolygonDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AlertDto(
        UUID idAlert,
        String title,
        AlertType alertType,
        Priority priority,
        RecordType recordType,
        String description,
        LocalDateTime createdAt,
        UUID authorId,
        List<String> images,
        UUID relatedLotId,
        UUID relatedCropId,
        UUID relatedAnimalId,
        PolygonDto polygonLimit,
        PointDto centroid
) {}
