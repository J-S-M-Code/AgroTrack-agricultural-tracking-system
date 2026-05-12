package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.CropJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CropJpaRepository extends JpaRepository<CropJpaEntity, UUID> {

    // Un cultivo activo es aquel que está en ese lote y su estado NO es HARVESTED (Cosechado)
    @Query("SELECT c FROM CropJpaEntity c WHERE c.lot.id = :lotId AND c.phenologicalState != 'HARVESTED'")
    List<CropJpaEntity> findActiveCropsByLotId(@Param("lotId") UUID lotId);
}