package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.port.in.farm.DeleteFarmUseCase;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.domain.port.out.task.TaskRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DeleteFarmService implements DeleteFarmUseCase {

    private final FarmRepositoryPort farmRepositoryPort;
    private final TaskRepositoryPort taskRepositoryPort;

    public DeleteFarmService(FarmRepositoryPort farmRepositoryPort, TaskRepositoryPort taskRepositoryPort) {
        this.farmRepositoryPort = farmRepositoryPort;
        this.taskRepositoryPort = taskRepositoryPort;
    }

    @Override
    @Transactional
    public void executeDeleteFarm(UUID farmId, String reason) {
        Farm farm = farmRepositoryPort.findById(farmId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Finca no encontrada con ID: " + farmId));

        farm.markAsDeleted(reason);
        farmRepositoryPort.save(farm);

        // Al borrar la finca, también "borramos" lógicamente todas sus tareas asociadas
        List<com.agrotrack.domain.model.entities.Task> tasks = taskRepositoryPort.findByFarmId(farmId);
        for (com.agrotrack.domain.model.entities.Task task : tasks) {
            task.setActive(false);
            task.setDeletionReason("Finca eliminada");
            taskRepositoryPort.save(task);
        }
    }
}
