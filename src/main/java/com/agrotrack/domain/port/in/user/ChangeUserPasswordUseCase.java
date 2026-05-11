package com.agrotrack.domain.port.in.user;

import java.util.UUID;

public interface ChangeUserPasswordUseCase {
    void execute(UUID userId, String currentRawPassword, String newRawPassword);
}