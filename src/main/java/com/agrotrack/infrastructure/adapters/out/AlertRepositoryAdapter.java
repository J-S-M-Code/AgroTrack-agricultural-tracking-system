package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Alert;
import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.port.out.alert.AlertRepositoryPort;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.AlertJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.AnimalJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.CropJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.LotJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.UserJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.AlertJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AlertRepositoryAdapter implements AlertRepositoryPort {

    private final AlertJpaRepository alertJpaRepository;
    private final FarmRepositoryPort farmRepositoryPort;

    public AlertRepositoryAdapter(AlertJpaRepository alertJpaRepository, FarmRepositoryPort farmRepositoryPort) {
        this.alertJpaRepository = alertJpaRepository;
        this.farmRepositoryPort = farmRepositoryPort;
    }

    @Override
    public Alert save(Alert alert) {
        // 1. Mapear Usuario Autor (Obligatorio)
        UserJpaEntity authorEntity = new UserJpaEntity();
        authorEntity.setId(alert.getAuthor().getIdUser());

        // 2. Mapear Lote Relacionado (Opcional)
        LotJpaEntity lotEntity = null;
        if (alert.getRelatedLot() != null) {
            lotEntity = new LotJpaEntity();
            lotEntity.setId(alert.getRelatedLot().getIdLot());
        }

        // 3. Mapear Cultivo Relacionado (Opcional)
        CropJpaEntity cropEntity = null;
        if (alert.getRelatedCrop() != null) {
            cropEntity = new CropJpaEntity();
            cropEntity.setId(alert.getRelatedCrop().getIdCrop());
        }

        // 4. Mapear Animal Relacionado (Opcional)
        AnimalJpaEntity animalEntity = null;
        if (alert.getRelatedAnimal() != null) {
            animalEntity = new AnimalJpaEntity();
            animalEntity.setId(alert.getRelatedAnimal().getIdAnimal());
        }

        // 5. Construir la entidad JPA
        AlertJpaEntity entity = new AlertJpaEntity(
                alert.getIdAlert(),
                alert.getTitle(),
                alert.getAlertType(),
                alert.getPriority(),
                alert.getRecordType(),
                alert.getDescription(),
                alert.getCreatedAt(),
                authorEntity,
                alert.getImages() != null ? new ArrayList<>(alert.getImages()) : new ArrayList<>(),
                lotEntity,
                cropEntity,
                animalEntity,
                alert.getPolygonLimit(),
                alert.getCentroid()
        );

        // 6. Guardar en Base de Datos y asignar UUID
        AlertJpaEntity savedEntity = alertJpaRepository.save(entity);
        alert.setIdAlert(savedEntity.getId());

        return alert;
    }

    @Override
    public Optional<Alert> findById(UUID alertId) {
        return alertJpaRepository.findById(alertId).map(this::mapToDomain);
    }

    @Override
    public List<Alert> findByLotId(UUID lotId) {
        return alertJpaRepository.findByRelatedLotId(lotId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Alert> findActiveAlertsByPriority(Priority priority) {
        return alertJpaRepository.findByPriority(priority).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Alert> findByFarmId(UUID farmId) {
        return alertJpaRepository.findByFarmId(farmId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    // --- Método auxiliar para convertir de Base de Datos a Dominio ---
    private Alert mapToDomain(AlertJpaEntity entity) {

        // Reconstruir Autor
        User author = User.create(
                entity.getAuthor().getName(), entity.getAuthor().getLastName(),
                entity.getAuthor().getDni(), entity.getAuthor().getPhone(),
                entity.getAuthor().getAddress(), entity.getAuthor().getEmail(),
                new Password("Dummy@2026"), entity.getAuthor().getRole()
        );
        author.setIdUser(entity.getAuthor().getId());

        // Reconstruir Lote (si existe)
        Lot lot = null;
        if (entity.getRelatedLot() != null) {
            lot = Lot.create(
                    entity.getRelatedLot().getName(), entity.getRelatedLot().getHectares(),
                    entity.getRelatedLot().getSoilType(), entity.getRelatedLot().getType(),
                    entity.getRelatedLot().getDescription(), entity.getRelatedLot().getPolygonLimit(), farmRepositoryPort.findById(entity.getRelatedLot().getFarm().getId()).orElse(null)
            );
            lot.setIdLot(entity.getRelatedLot().getId());
        }

        // Reconstruir Cultivo (si existe)
        Crop crop = null;
        if (entity.getRelatedCrop() != null) {
            Lot cropLot = null;
            if (entity.getRelatedCrop().getLot() != null) {
                cropLot = Lot.create(
                        entity.getRelatedCrop().getLot().getName(), entity.getRelatedCrop().getLot().getHectares(),
                        entity.getRelatedCrop().getLot().getSoilType(), entity.getRelatedCrop().getLot().getType(),
                        entity.getRelatedCrop().getLot().getDescription(), entity.getRelatedCrop().getLot().getPolygonLimit(), farmRepositoryPort.findById(entity.getRelatedCrop().getLot().getFarm().getId()).orElse(null)
                );
                cropLot.setIdLot(entity.getRelatedCrop().getLot().getId());
            }
            crop = Crop.create(
                    entity.getRelatedCrop().getTypeCrop(), entity.getRelatedCrop().getSpecies(),
                    entity.getRelatedCrop().getVariety(), entity.getRelatedCrop().getPlantingDate(),
                    entity.getRelatedCrop().getEstimateHarvestDate(), cropLot,
                    entity.getRelatedCrop().getImplantedSurface(), entity.getRelatedCrop().getRenspa(),
                    entity.getRelatedCrop().getPhenologicalState()
            );
            crop.setIdCrop(entity.getRelatedCrop().getId());
        }

        // Reconstruir Animal (si existe)
        Animal animal = null;
        if (entity.getRelatedAnimal() != null) {
            Lot animalLot = null;
            if (entity.getRelatedAnimal().getAssignedLot() != null) {
                animalLot = Lot.create(
                        entity.getRelatedAnimal().getAssignedLot().getName(), entity.getRelatedAnimal().getAssignedLot().getHectares(),
                        entity.getRelatedAnimal().getAssignedLot().getSoilType(), entity.getRelatedAnimal().getAssignedLot().getType(),
                        entity.getRelatedAnimal().getAssignedLot().getDescription(), entity.getRelatedAnimal().getAssignedLot().getPolygonLimit(), farmRepositoryPort.findById(entity.getRelatedAnimal().getAssignedLot().getFarm().getId()).orElse(null)
                );
                animalLot.setIdLot(entity.getRelatedAnimal().getAssignedLot().getId());
            }
            animal = Animal.create(
                    entity.getRelatedAnimal().getVisualCaravan(), entity.getRelatedAnimal().getCaravanSenasa(),
                    entity.getRelatedAnimal().getLivestockKey(), entity.getRelatedAnimal().getNumRENSPA(),
                    entity.getRelatedAnimal().getInternalManagementCaravan(), entity.getRelatedAnimal().getSpecies(),
                    entity.getRelatedAnimal().getRace(), entity.getRelatedAnimal().getSex(),
                    entity.getRelatedAnimal().getCategory(), entity.getRelatedAnimal().getBirthdate(),
                    entity.getRelatedAnimal().getCurrentWeight(), animalLot
            );
            animal.setIdAnimal(entity.getRelatedAnimal().getId());
        }

        // Reconstruir la Alerta Final
        Alert alert = Alert.create(
                entity.getTitle(), entity.getAlertType(), entity.getPriority(),
                entity.getRecordType(), entity.getDescription(), entity.getCreatedAt(),
                author, new ArrayList<>(entity.getImages()), lot, crop, animal,
                entity.getPolygonLimit(), entity.getCentroid()
        );
        alert.setIdAlert(entity.getId());

        return alert;
    }
}