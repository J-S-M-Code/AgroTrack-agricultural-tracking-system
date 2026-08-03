package com.agrotrack.infrastructure.adapters.in.web.dto;

import com.agrotrack.domain.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
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
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
}
