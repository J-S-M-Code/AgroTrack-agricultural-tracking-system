package com.agrotrack.domain.port.out.animal;

import com.agrotrack.domain.model.entities.HealthEvent;

import java.util.List;
import java.util.UUID;

public interface HealthEventRepositoryPort {
    HealthEvent save(HealthEvent healthEvent);
    List<HealthEvent> findByAnimalId(UUID animalId);
}
