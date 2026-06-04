package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.UserDto;
import com.agrotrack.domain.port.in.user.GetPersonnelByFarmUseCase;
import com.agrotrack.infrastructure.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetPersonnelByFarmUseCase getPersonnelByFarmUseCase;

    public UserController(GetPersonnelByFarmUseCase getPersonnelByFarmUseCase) {
        this.getPersonnelByFarmUseCase = getPersonnelByFarmUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // En una app real mapearíamos el User de userDetails a UserDto
        // Por simplicidad devolvemos datos básicos o usamos un mapper inyectado
        com.agrotrack.domain.model.entities.User user = userDetails.getUser();
        UserDto dto = UserDto.builder()
                .idUser(user.getIdUser())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .dni(user.getDni())
                .build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST')")
    public ResponseEntity<List<UserDto>> getPersonnelByFarm(@PathVariable UUID farmId) {
        // Solo OWNER y AGRONOMIST pueden ver el personal de la finca.
        // Opcional: Validar que el usuario autenticado pertenezca a esa finca
        List<UserDto> users = getPersonnelByFarmUseCase.executeGetPersonnelByFarm(farmId);
        return ResponseEntity.ok(users);
    }
}
