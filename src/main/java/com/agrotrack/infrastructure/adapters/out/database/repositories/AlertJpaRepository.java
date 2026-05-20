package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.AlertJpaEntity;
import com.agrotrack.domain.model.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, UUID> {
    List<AlertJpaEntity> findByRelatedLotId(UUID lotId);
    List<AlertJpaEntity> findByPriority(Priority priority);
}