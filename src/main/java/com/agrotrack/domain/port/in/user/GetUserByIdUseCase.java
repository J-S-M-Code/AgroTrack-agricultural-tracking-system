package com.agrotrack.domain.port.in.user;

import com.agrotrack.application.dto.UserDto;
import java.util.UUID;

public interface GetUserByIdUseCase {
    UserDto executeGetUserById(UUID userId);
}
