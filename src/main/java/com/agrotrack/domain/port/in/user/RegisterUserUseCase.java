package com.agrotrack.domain.port.in.user;

import com.agrotrack.domain.model.entities.User;

public interface RegisterUserUseCase {
    User execute(User user);
}
