package com.agrotrack.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileDto {
    @NotBlank
    private String name;
    
    @NotBlank
    private String lastName;
    
    @NotBlank
    private String phone;
    
    @NotBlank
    private String address;
}
