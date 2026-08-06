package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.CreateUserDto;
import com.agrotrack.application.dto.UserDto;
import com.agrotrack.domain.port.in.user.GetPersonnelByFarmUseCase;
import com.agrotrack.domain.port.in.user.InviteUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farms/{farmId}/users")
public class FarmUserController {

    private final GetPersonnelByFarmUseCase getPersonnelByFarmUseCase;
    private final InviteUserUseCase inviteUserUseCase;

    public FarmUserController(GetPersonnelByFarmUseCase getPersonnelByFarmUseCase, InviteUserUseCase inviteUserUseCase) {
        this.getPersonnelByFarmUseCase = getPersonnelByFarmUseCase;
        this.inviteUserUseCase = inviteUserUseCase;
    }

    @GetMapping
    @PreAuthorize("hasPermission(#farmId, 'OWNER')")
    public ResponseEntity<List<UserDto>> getPersonnelByFarm(@PathVariable UUID farmId) {
        List<UserDto> users = getPersonnelByFarmUseCase.executeGetPersonnelByFarm(farmId);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @PreAuthorize("hasPermission(#farmId, 'OWNER')")
    public ResponseEntity<Void> inviteUser(@PathVariable UUID farmId, @Valid @RequestBody CreateUserDto userDto) {
        inviteUserUseCase.executeInviteUser(userDto, farmId);
        return ResponseEntity.ok().build();
    }
}
