package com.agrotrack.domain.port.in.user;

import java.util.UUID;

public interface DeleteUserUseCase {
    void executeDeleteUser(UUID userId);
}
