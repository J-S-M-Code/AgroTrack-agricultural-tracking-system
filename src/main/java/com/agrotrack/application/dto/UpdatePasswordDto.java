package com.agrotrack.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordDto {
    @NotBlank
    private String currentPassword;
    
    @NotBlank
    private String newPassword;
}
