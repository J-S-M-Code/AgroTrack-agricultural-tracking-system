package com.agrotrack.application.dto;

import java.time.LocalDateTime;

public record GPSPositionDto(
        Double latitude,
        Double longitude,
        LocalDateTime timestamp
) {}
