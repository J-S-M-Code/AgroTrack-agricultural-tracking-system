package com.agrotrack.domain.port.in.user;

import java.util.List;
import java.util.UUID;

public interface AssignPersonnelUseCase {
    void executeAssignPersonnel(String email, String role, List<UUID> farmIds);
}
