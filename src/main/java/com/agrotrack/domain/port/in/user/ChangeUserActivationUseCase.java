package com.agrotrack.domain.port.in.user;

import java.util.UUID;

public interface ChangeUserActivationUseCase {
    void executeChangeUserActivation(UUID userId, boolean newStatus);
}