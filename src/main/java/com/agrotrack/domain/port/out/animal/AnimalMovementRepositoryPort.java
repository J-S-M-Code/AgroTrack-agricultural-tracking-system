package com.agrotrack.domain.port.out.animal;


import com.agrotrack.domain.model.entities.AnimalMovement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnimalMovementRepositoryPort {
    AnimalMovement save(AnimalMovement movement);
    Optional<AnimalMovement> findActiveMovementByAnimalId(UUID animalId);
    List<AnimalMovement> findAllByAnimalId(UUID animalId);
}