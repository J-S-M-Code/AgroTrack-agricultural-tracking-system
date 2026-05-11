package com.agrotrack.domain.port.out.animal;

import com.agrotrack.domain.model.entities.Animal;
import java.util.Optional;
import java.util.UUID;

public interface AnimalRepositoryPort {
    Animal save(Animal animal);
    Optional<Animal> findById(UUID animalId);
    Optional<Animal> findByCaravanSenasa(String caravanSenasa);
    boolean existsByCaravanSenasa(String caravanSenasa);
}
