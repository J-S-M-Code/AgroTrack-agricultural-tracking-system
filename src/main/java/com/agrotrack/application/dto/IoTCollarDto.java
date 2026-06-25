package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.State;

public record IoTCollarDto(
        String codeRFID,
        String model,
        State state,
        Double batteryLevel
) {}
