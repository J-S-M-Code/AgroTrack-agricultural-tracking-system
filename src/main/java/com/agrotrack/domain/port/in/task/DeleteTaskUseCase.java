package com.agrotrack.domain.port.in.task;

import java.util.UUID;

public interface DeleteTaskUseCase {
    void executeDeleteTask(UUID taskId, String reason);
}
