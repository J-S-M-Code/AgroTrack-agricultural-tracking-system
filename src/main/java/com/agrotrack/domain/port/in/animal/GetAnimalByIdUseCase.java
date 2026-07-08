package com.agrotrack.domain.port.in.animal;

import com.agrotrack.domain.model.entities.Animal;
import java.util.UUID;
import java.util.Optional;

public interface GetAnimalByIdUseCase {
    Optional<Animal> executeGetAnimalById(UUID animalId);
}
