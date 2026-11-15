package com.agrotrack.domain.port.in.task;

import com.agrotrack.application.dto.TaskDto;
import com.agrotrack.domain.model.enums.TaskStatus;
import java.util.List;
import java.util.UUID;

public interface UpdateTaskStatusUseCase {
    default void executeUpdateTaskStatus(UUID taskId, TaskStatus newStatus) {
        executeUpdateTaskStatus(taskId, newStatus, null, null);
    }

    TaskDto executeUpdateTaskStatus(UUID taskId, TaskStatus newStatus, String completionNotes, List<String> completionImages);
}
