package com.agrotrack.domain.port.in.crop;

import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterHarvestUseCase {
    void execute(UUID cropId, LocalDateTime harvestDate);
}
