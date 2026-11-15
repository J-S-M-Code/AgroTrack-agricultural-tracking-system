package com.agrotrack.domain.port.in.user;

import com.agrotrack.application.dto.CreateUserDto;
import java.util.UUID;

public interface InviteUserUseCase {
    void executeInviteUser(CreateUserDto userDto, UUID farmId);
}
