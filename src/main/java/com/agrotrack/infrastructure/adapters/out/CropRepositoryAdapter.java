package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.port.out.crop.CropRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.CropJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.LotJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.CropJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CropRepositoryAdapter implements CropRepositoryPort {

    private final CropJpaRepository cropJpaRepository;

    public CropRepositoryAdapter(CropJpaRepository cropJpaRepository) {
        this.cropJpaRepository = cropJpaRepository;
    }

    @Override
    public Crop save(Crop crop) {
        LotJpaEntity lotEntity = new LotJpaEntity();
        lotEntity.setId(crop.getAssignedLot().getIdLot());

        CropJpaEntity entity = new CropJpaEntity(
                crop.getIdCrop(),
                crop.getTypeCrop(),
                crop.getSpecies(),
                crop.getVariety(),
                crop.getPlantingDate(),
                crop.getEstimateHarvestDate(),
                crop.getEstimateHarvestDate(),
                crop.getImplantedSurface(),
                crop.getRenspa(),
                crop.getPhenologicalState(),
                lotEntity
        );

        CropJpaEntity savedEntity = cropJpaRepository.save(entity);
        crop.setIdCrop(savedEntity.getId());
        return crop;
    }

    @Override
    public Optional<Crop> findById(UUID cropId) {
        return cropJpaRepository.findById(cropId).map(this::mapToDomain);
    }

    @Override
    public List<Crop> findActiveCropsByLotId(UUID lotId) {
        return cropJpaRepository.findActiveCropsByLotId(lotId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private Crop mapToDomain(CropJpaEntity entity) {
        // Reconstruimos un Lote básico para que el dominio no falle por nulos
        Lot lot = Lot.create(
                entity.getLot().getName(), entity.getLot().getHectares(), entity.getLot().getSoilType(),
                entity.getLot().getType(), entity.getLot().getDescription(), entity.getLot().getPolygonLimit()
        );
        lot.setIdLot(entity.getLot().getId());

        Crop crop = Crop.create(
                entity.getTypeCrop(), entity.getSpecies(), entity.getVariety(),
                entity.getPlantingDate(), entity.getEstimateHarvestDate(),
                lot, entity.getImplantedSurface(), entity.getRenspa(), entity.getPhenologicalState()
        );
        crop.setIdCrop(entity.getId());
        if (entity.getHarvestDate() != null) {
            crop.setHarvestDate(entity.getHarvestDate());
        }
        return crop;
    }
}