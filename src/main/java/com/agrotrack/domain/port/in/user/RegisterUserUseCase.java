package com.agrotrack.domain.port.in.user;

import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.UserRole;

public interface RegisterUserUseCase {
    User executeRegisterUser(String name, String lastName, String dni, String phone,
                 String address, String email, String rawPassword);
}