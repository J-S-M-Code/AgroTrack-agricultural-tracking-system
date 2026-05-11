package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Task;
import com.agrotrack.domain.model.enums.TaskStatus;
import com.agrotrack.domain.port.out.task.TaskRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskRepositoryAdapter implements TaskRepositoryPort {
    @Override public Task save(Task task) { return task; }
    @Override public Optional<Task> findById(UUID taskId) { return Optional.empty(); }
    @Override public List<Task> findByFarmId(UUID farmId) { return List.of(); }
    @Override public List<Task> findByAssignedUserIdAndStatus(UUID assignedUserId, TaskStatus status) { return List.of(); }
}