package com.agrotrack.domain.port.out.animal;

import com.agrotrack.domain.model.entities.AnimalMovement;

import java.util.List;
import java.util.UUID;

public interface AnimalMovementRepoisitoryPort {
    AnimalMovement save(AnimalMovement animalMovement);
    List<AnimalMovement> findMovementByAnimalId(UUID id);
}
