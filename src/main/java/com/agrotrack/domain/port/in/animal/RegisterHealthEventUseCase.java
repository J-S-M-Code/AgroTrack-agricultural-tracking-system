package com.agrotrack.domain.port.in.animal;

import com.agrotrack.domain.model.enums.HealthEventType;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterHealthEventUseCase {
    void execute(UUID animalId, LocalDateTime date, HealthEventType type, String treatment,
                 String numAct, String observation, UUID veterinarianId);
}