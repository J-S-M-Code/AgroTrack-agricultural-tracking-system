package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.TaskJpaEntity;
import com.agrotrack.domain.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskJpaRepository extends JpaRepository<TaskJpaEntity, UUID> {
    List<TaskJpaEntity> findByFarmId(UUID farmId);
    List<TaskJpaEntity> findByAssignedId(UUID assignedId);
    List<TaskJpaEntity> findByAssignedIdAndTaskStatus(UUID assignedId, TaskStatus taskStatus);
    long countByFarmIdAndAssignedIdAndTaskStatus(UUID farmId, UUID assignedId, TaskStatus taskStatus);
}