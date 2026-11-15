package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FarmAccess {
    private UUID farmId;
    private String farmName;
    private UserRole role;
}
