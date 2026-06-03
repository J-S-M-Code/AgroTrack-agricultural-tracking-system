package com.agrotrack.domain.port.in.user;

import com.agrotrack.application.dto.UserDto;
import java.util.List;
import java.util.UUID;

public interface GetPersonnelByFarmUseCase {
    List<UserDto> executeGetPersonnelByFarm(UUID farmId);
}
