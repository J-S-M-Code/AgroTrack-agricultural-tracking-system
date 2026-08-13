package com.agrotrack.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptInviteDto {
    @NotBlank
    private String token;
    
    @NotBlank
    private String newPassword;
}
