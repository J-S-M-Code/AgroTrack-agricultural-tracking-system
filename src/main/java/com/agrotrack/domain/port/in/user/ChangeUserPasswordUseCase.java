package com.agrotrack.domain.port.in.user;

import java.util.UUID;

public interface ChangeUserPasswordUseCase {
    void executeChangeUserPassword(UUID userId, String currentRawPassword, String newRawPassword);
}