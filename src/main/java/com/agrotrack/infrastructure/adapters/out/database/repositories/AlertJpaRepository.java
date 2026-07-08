package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.AlertJpaEntity;
import com.agrotrack.domain.model.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, UUID> {
    List<AlertJpaEntity> findByRelatedLotId(UUID lotId);
    List<AlertJpaEntity> findByPriority(Priority priority);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM AlertJpaEntity a LEFT JOIN a.relatedLot l LEFT JOIN a.relatedCrop c LEFT JOIN c.lot cl LEFT JOIN a.relatedAnimal an LEFT JOIN an.assignedLot anl WHERE l.farm.id = :farmId OR cl.farm.id = :farmId OR anl.farm.id = :farmId")
    List<AlertJpaEntity> findByFarmId(@org.springframework.data.repository.query.Param("farmId") UUID farmId);
}