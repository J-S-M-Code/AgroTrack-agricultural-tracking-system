package com.agrotrack.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FarmContextDto {
    private long pendingTasks;
    private long totalAlerts;
}
