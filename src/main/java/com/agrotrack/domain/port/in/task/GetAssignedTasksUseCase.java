package com.agrotrack.domain.port.in.task;

import com.agrotrack.application.dto.TaskDto;
import java.util.List;
import java.util.UUID;

public interface GetAssignedTasksUseCase {
    List<TaskDto> executeGetAssignedTasks(UUID userId);
}
