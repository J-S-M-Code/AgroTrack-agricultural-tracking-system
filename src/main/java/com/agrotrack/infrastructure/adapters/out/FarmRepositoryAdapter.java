package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.FarmJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.FarmJpaRepository;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FarmRepositoryAdapter implements FarmRepositoryPort {

    private final FarmJpaRepository farmJpaRepository;

    public FarmRepositoryAdapter(FarmJpaRepository farmJpaRepository) {
        this.farmJpaRepository = farmJpaRepository;
    }

    @Override
    public Farm save(Farm farm) {
        // 1. Mapeamos la entidad "Pura" de dominio a una entidad de Base de Datos
        FarmJpaEntity entity = new FarmJpaEntity();
        entity.setId(farm.getIdFarm());
        entity.setName(farm.getName());
        entity.setCompanyName(farm.getCompanyName());
        entity.setCuit(farm.getCuit());
        entity.setNumberRENAPSA(farm.getNumberRENAPSA());
        entity.setProductiveOrientation(farm.getProductiveOrientation());
        entity.setAddress(farm.getAddress());
        entity.setPolygonLimit(farm.getPolygonLimit());
        entity.setCentroid(farm.getCentroid());
        entity.setSurface(farm.getSurface());
        entity.setImageUrl(farm.getImageUrl());
        entity.setActive(farm.isActive());
        entity.setDeletionReason(farm.getDeletionReason());
        entity.setDeletionDate(farm.getDeletionDate());

        // 2. Guardamos. Aquí PostgreSQL inserta la fila y genera el UUID.
        FarmJpaEntity savedEntity = farmJpaRepository.save(entity);

        // 3. Le pasamos el UUID recién nacido a nuestra entidad de dominio
        farm.setIdFarm(savedEntity.getId());

        return farm; // Retornamos la entidad con su nuevo ID
    }

    @Override
    public Optional<Farm> findById(UUID id) {
        return farmJpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public boolean existsOverlappingFarm(Polygon newPerimeter, UUID excludeFarmId) {
        return farmJpaRepository.existsOverlappingFarm(newPerimeter, excludeFarmId);
    }

    @Override
    public boolean existsByCuit(String cuit) {
        return farmJpaRepository.existsByCuit(cuit);
    }

    @Override
    public List<Farm> findByUserId(UUID userId) {
        return farmJpaRepository.findFarmsByUserId(userId).stream().map(this::mapToDomain).toList();
    }

    // --- Método auxiliar para cuando recuperamos datos de la BD ---
    private Farm mapToDomain(FarmJpaEntity entity) {
        Farm farm = Farm.create(
                entity.getName(),
                entity.getCompanyName(),
                entity.getCuit(),
                entity.getNumberRENAPSA(),
                entity.getProductiveOrientation(),
                entity.getAddress(),
                entity.getPolygonLimit(),
                entity.getCentroid(),
                entity.getSurface(),
                entity.getImageUrl()
        );
        farm.setIdFarm(entity.getId());
        farm.setActive(entity.isActive());
        farm.setDeletionReason(entity.getDeletionReason());
        farm.setDeletionDate(entity.getDeletionDate());
        return farm;
    }
}