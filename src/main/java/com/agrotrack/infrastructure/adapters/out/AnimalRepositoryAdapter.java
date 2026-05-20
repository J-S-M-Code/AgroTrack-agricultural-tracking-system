package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.AnimalJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.LotJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.AnimalJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AnimalRepositoryAdapter implements AnimalRepositoryPort {

    private final AnimalJpaRepository animalJpaRepository;

    public AnimalRepositoryAdapter(AnimalJpaRepository animalJpaRepository) {
        this.animalJpaRepository = animalJpaRepository;
    }

    @Override
    public Animal save(Animal animal) {
        LotJpaEntity lotEntity = null;
        if (animal.getAssignedLot() != null) {
            lotEntity = new LotJpaEntity();
            lotEntity.setId(animal.getAssignedLot().getIdLot());
        }

        AnimalJpaEntity entity = new AnimalJpaEntity(
                animal.getIdAnimal(),
                animal.getVisualCaravan(),
                animal.getCaravanSenasa(),
                animal.getLivestockKey(),
                animal.getNumRENSPA(),
                animal.getInternalManagementCaravan(),
                animal.getSpecies(),
                animal.getRace(),
                animal.getSex(),
                animal.getCategory(),
                animal.getBirthdate(),
                animal.getCurrentWeight(),
                lotEntity,
                new ArrayList<>(), // Se delega al cascade o al repositorio de movimientos
                new ArrayList<>()  // Se delega al cascade o al repositorio de salud
        );

        AnimalJpaEntity savedEntity = animalJpaRepository.save(entity);
        animal.setIdAnimal(savedEntity.getId());
        return animal;
    }

    @Override
    public Optional<Animal> findById(UUID animalId) {
        return animalJpaRepository.findById(animalId).map(this::mapToDomain);
    }

    @Override
    public Optional<Animal> findByCaravanSenasa(String caravanSenasa) {
        return animalJpaRepository.findByCaravanSenasa(caravanSenasa).map(this::mapToDomain);
    }

    @Override
    public boolean existsByCaravanSenasa(String caravanSenasa) {
        return animalJpaRepository.existsByCaravanSenasa(caravanSenasa);
    }

    private Animal mapToDomain(AnimalJpaEntity entity) {
        Lot lot = null;
        if (entity.getAssignedLot() != null) {
            lot = Lot.create(
                    entity.getAssignedLot().getName(), entity.getAssignedLot().getHectares(),
                    entity.getAssignedLot().getSoilType(), entity.getAssignedLot().getType(),
                    entity.getAssignedLot().getDescription(), entity.getAssignedLot().getPolygonLimit()
            );
            lot.setIdLot(entity.getAssignedLot().getId());
        }

        Animal animal = Animal.create(
                entity.getVisualCaravan(), entity.getCaravanSenasa(), entity.getLivestockKey(),
                entity.getNumRENSPA(), entity.getInternalManagementCaravan(), entity.getSpecies(),
                entity.getRace(), entity.getSex(), entity.getCategory(), entity.getBirthdate(),
                entity.getCurrentWeight(), lot
        );
        animal.setIdAnimal(entity.getId());
        return animal;
    }
}