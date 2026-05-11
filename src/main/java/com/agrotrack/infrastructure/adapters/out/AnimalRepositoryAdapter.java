package com.agrotrack.infrastructure.adapters.out;
import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AnimalRepositoryAdapter implements AnimalRepositoryPort {
    @Override public Animal save(Animal animal) { return animal; }
    @Override public Optional<Animal> findById(UUID animalId) { return Optional.empty(); }
    @Override public Optional<Animal> findByCaravanSenasa(String caravanSenasa) { return Optional.empty(); }
    @Override public boolean existsByCaravanSenasa(String caravanSenasa) { return false; }
}