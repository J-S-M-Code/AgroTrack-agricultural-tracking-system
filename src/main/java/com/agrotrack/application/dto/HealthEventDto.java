package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.HealthEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record HealthEventDto(
        LocalDateTime date,
        HealthEventType type,
        String treatment,
        String numAct,
        String observation,
        UUID veterinarianId
) {}
