package com.agrotrack.domain.port.in.task;

import com.agrotrack.domain.model.enums.TaskStatus;
import java.util.UUID;

public interface UpdateTaskStatusUseCase {
    void executeUpdateTaskStatus(UUID taskId, TaskStatus newStatus);
}