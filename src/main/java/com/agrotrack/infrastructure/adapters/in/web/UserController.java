package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.UserDto;
import com.agrotrack.domain.port.in.user.GetPersonnelByFarmUseCase;
import com.agrotrack.domain.port.in.user.GetUserByIdUseCase;
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
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final com.agrotrack.application.mapper.ApplicationDtoMapper mapper;

    public UserController(GetPersonnelByFarmUseCase getPersonnelByFarmUseCase,
                          GetUserByIdUseCase getUserByIdUseCase,
                          com.agrotrack.application.mapper.ApplicationDtoMapper mapper) {
        this.getPersonnelByFarmUseCase = getPersonnelByFarmUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        com.agrotrack.domain.model.entities.User user = userDetails.getUser();
        return ResponseEntity.ok(mapper.toUserDto(user));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST')")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(getUserByIdUseCase.executeGetUserById(id));
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST')")
    public ResponseEntity<List<UserDto>> getPersonnelByFarm(@PathVariable UUID farmId) {
        List<UserDto> users = getPersonnelByFarmUseCase.executeGetPersonnelByFarm(farmId);
        return ResponseEntity.ok(users);
    }
}
