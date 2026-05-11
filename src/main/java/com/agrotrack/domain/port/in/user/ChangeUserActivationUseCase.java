package com.agrotrack.domain.port.in.user;

import java.util.UUID;

public interface ChangeUserActivationUseCase {
    void execute(UUID userId, boolean newStatus);
}