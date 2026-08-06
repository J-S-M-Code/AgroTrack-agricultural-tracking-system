package com.agrotrack.application.services;

import com.agrotrack.application.dto.FarmContextDto;
import com.agrotrack.domain.port.in.user.GetFarmContextUseCase;
import com.agrotrack.domain.port.out.alert.AlertRepositoryPort;
import com.agrotrack.domain.port.out.task.TaskRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FarmContextService implements GetFarmContextUseCase {

    private final TaskRepositoryPort taskRepository;
    private final AlertRepositoryPort alertRepository;

    public FarmContextService(TaskRepositoryPort taskRepository, AlertRepositoryPort alertRepository) {
        this.taskRepository = taskRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public FarmContextDto executeGetFarmContext(UUID userId, UUID farmId) {
        long pendingTasks = taskRepository.countPendingByFarmAndUser(farmId, userId);
        long alerts = alertRepository.countByFarmId(farmId);
        return FarmContextDto.builder()
                .pendingTasks(pendingTasks)
                .totalAlerts(alerts)
                .build();
    }
}
