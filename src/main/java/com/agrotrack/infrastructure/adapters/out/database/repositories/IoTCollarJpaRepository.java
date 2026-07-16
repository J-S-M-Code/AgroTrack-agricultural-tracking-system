package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.domain.model.enums.State;
import com.agrotrack.infrastructure.adapters.out.database.entities.IoTCollarJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IoTCollarJpaRepository extends JpaRepository<IoTCollarJpaEntity, UUID> {
    Optional<IoTCollarJpaEntity> findByCodeRFID(String codeRFID);
    List<IoTCollarJpaEntity> findByState(State state);
    List<IoTCollarJpaEntity> findByBatteryLevelLessThan(Double batteryThreshold);
    List<IoTCollarJpaEntity> findByFarm_Id(UUID farmId);
}
