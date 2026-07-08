package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserDto {
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private UUID idUser;
    private String name;
    private String lastName;
    private String dni;
    private String phone;
    private String address;
    private String email;
    // IMPORTANTE: Omitimos el password aquí por seguridad
    private UserRole role;
    private LocalDateTime creationDate;
    private LocalDateTime lastAccess;
    private boolean active;
}
