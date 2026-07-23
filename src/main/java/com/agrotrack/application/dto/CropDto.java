package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.PhenologicalState;
import com.agrotrack.domain.model.enums.TypeCrop;
import java.time.LocalDateTime;

import java.util.UUID;

public record CropDto(
        UUID idCrop,
        UUID lotId,
        String lotName,
        UUID farmId,
        String farmName,
        TypeCrop typeCrop,
        String species,
        String variety,
        LocalDateTime plantingDate,
        LocalDateTime estimateHarvestDate,
        double implantedSurface,
        String renspa,
        PhenologicalState phenologicalState
) {}
