package com.agrotrack.domain.port.in.user;

import com.agrotrack.domain.model.enums.UserRole;
import java.util.List;
import java.util.UUID;

public interface UpdateUserAssignmentUseCase {
    void executeUpdateUserAssignment(UUID userId, UserRole newRole, List<UUID> farmIds);
}
