package com.agrotrack.domain.port.out.task;

import com.agrotrack.domain.model.entities.Task;
import com.agrotrack.domain.model.enums.TaskStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepositoryPort {
    Task save(Task task);
    Optional<Task> findById(UUID taskId);
    List<Task> findByFarmId(UUID farmId);
    List<Task> findByAssignedUserIdAndStatus(UUID assignedUserId, TaskStatus status);
}