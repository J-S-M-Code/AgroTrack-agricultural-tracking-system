package com.agrotrack.domain.port.in.task;

import com.agrotrack.application.dto.TaskDto;
import java.util.UUID;

public interface GetTaskByIdUseCase {
    TaskDto executeGetTaskById(UUID taskId);
}
