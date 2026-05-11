package com.agrotrack.domain.port.out.animal;

import com.agrotrack.domain.model.entities.Animal;

import java.util.Optional;
import java.util.UUID;

public interface AnimalRepositoryPort {
    Animal save(Animal animal);
    Optional<Animal> findById(UUID id);
    Optional<Animal> findByCaravanaSenasa(String caravanaSenasa);
    Optional<Animal> findByRdifTag(String rdifTag);
    boolean existByCaravanaSenasa(String caravanaSenasa);
}
