package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserSummaryDto {
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private UUID idUser;
    private String name;
    private String lastName;
    private String email;
    private UserRole role;
    private boolean active;
}
