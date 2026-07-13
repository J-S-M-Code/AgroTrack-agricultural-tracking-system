package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.AnimalJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.IoTCollarJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.LotJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.AnimalJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AnimalRepositoryAdapter implements AnimalRepositoryPort {

    private final AnimalJpaRepository animalJpaRepository;
    private final FarmRepositoryPort farmRepositoryPort;

    public AnimalRepositoryAdapter(AnimalJpaRepository animalJpaRepository, FarmRepositoryPort farmRepositoryPort) {
        this.animalJpaRepository = animalJpaRepository;
        this.farmRepositoryPort = farmRepositoryPort;
    }

    @Override
    public Animal save(Animal animal) {
        LotJpaEntity lotEntity = null;
        if (animal.getAssignedLot() != null) {
            lotEntity = new LotJpaEntity();
            lotEntity.setId(animal.getAssignedLot().getIdLot());
        }

        IoTCollarJpaEntity collarJpaEntity = null;
        if (animal.getCollar() != null) {
            collarJpaEntity = new IoTCollarJpaEntity();
            collarJpaEntity.setId(animal.getCollar().getIdCollar());
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
                collarJpaEntity,
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

    @Override
    public Optional<Animal> findByCollarId(UUID collarId) {
        return animalJpaRepository.findByCollar_Id(collarId).map(this::mapToDomain);
    }

    @Override
    public java.util.List<Animal> findByFarmId(UUID farmId) {
        return animalJpaRepository.findByAssignedLot_Farm_Id(farmId).stream().map(this::mapToDomain).toList();
    }

    private Animal mapToDomain(AnimalJpaEntity entity) {
        Lot lot = null;
        if (entity.getAssignedLot() != null) {
            lot = Lot.create(
                    entity.getAssignedLot().getName(), entity.getAssignedLot().getHectares(),
                    entity.getAssignedLot().getSoilType(), entity.getAssignedLot().getType(),
                    entity.getAssignedLot().getDescription(), entity.getAssignedLot().getPolygonLimit(), farmRepositoryPort.findById(entity.getAssignedLot().getFarm().getId()).orElse(null)
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
        
        if (entity.getCollar() != null) {
            java.util.UUID collarFarmId = entity.getCollar().getFarm() != null ? entity.getCollar().getFarm().getId() : null;
            com.agrotrack.domain.model.entities.IoTCollar domainCollar = com.agrotrack.domain.model.entities.IoTCollar.create(
                    entity.getCollar().getCodeRFID(), entity.getCollar().getModel(),
                    entity.getCollar().getState(), entity.getCollar().getBatteryLevel(), collarFarmId
            );
            domainCollar.setIdCollar(entity.getCollar().getId());
            animal.assignCollar(domainCollar);
        }
        return animal;
    }
}