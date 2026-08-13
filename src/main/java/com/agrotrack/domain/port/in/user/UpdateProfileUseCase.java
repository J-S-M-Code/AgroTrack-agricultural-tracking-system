package com.agrotrack.domain.port.in.user;

import com.agrotrack.application.dto.UpdateProfileDto;
import java.util.UUID;

public interface UpdateProfileUseCase {
    void executeUpdateProfile(UUID userId, UpdateProfileDto dto);
}
