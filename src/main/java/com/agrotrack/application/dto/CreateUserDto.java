package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {
    @NotBlank
    private String name;
    
    @NotBlank
    private String lastName;
    
    @NotBlank
    private String dni;
    
    @NotBlank
    private String phone;
    
    @NotBlank
    private String address;
    
    @Email
    @NotBlank
    private String email;
    
    private String password; // Solo si sendEmail = false
    
    @NotNull
    private UserRole role;
    
    private boolean sendEmail;
}
