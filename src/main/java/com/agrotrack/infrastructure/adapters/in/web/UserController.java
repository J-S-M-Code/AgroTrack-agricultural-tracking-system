package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.UserDto;
import com.agrotrack.application.dto.UpdateProfileDto;
import com.agrotrack.application.dto.UpdatePasswordDto;
import com.agrotrack.domain.port.in.user.GetPersonnelByFarmUseCase;
import com.agrotrack.domain.port.in.user.GetUserByIdUseCase;
import com.agrotrack.domain.port.in.user.AssignPersonnelUseCase;
import com.agrotrack.domain.port.in.user.UpdateProfileUseCase;
import com.agrotrack.domain.port.in.user.ChangeUserPasswordUseCase;
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
    private final AssignPersonnelUseCase assignPersonnelUseCase;
    private final com.agrotrack.domain.port.in.user.UpdateUserAssignmentUseCase updateUserAssignmentUseCase;
    private final com.agrotrack.domain.port.in.user.DeleteUserUseCase deleteUserUseCase;
    private final com.agrotrack.domain.port.in.user.GetFarmContextUseCase getFarmContextUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final ChangeUserPasswordUseCase changeUserPasswordUseCase;
    private final com.agrotrack.application.mapper.ApplicationDtoMapper mapper;

    public UserController(GetPersonnelByFarmUseCase getPersonnelByFarmUseCase,
                          GetUserByIdUseCase getUserByIdUseCase,
                          AssignPersonnelUseCase assignPersonnelUseCase,
                          com.agrotrack.domain.port.in.user.UpdateUserAssignmentUseCase updateUserAssignmentUseCase,
                          com.agrotrack.domain.port.in.user.DeleteUserUseCase deleteUserUseCase,
                          com.agrotrack.domain.port.in.user.GetFarmContextUseCase getFarmContextUseCase,
                          UpdateProfileUseCase updateProfileUseCase,
                          ChangeUserPasswordUseCase changeUserPasswordUseCase,
                          com.agrotrack.application.mapper.ApplicationDtoMapper mapper) {
        this.getPersonnelByFarmUseCase = getPersonnelByFarmUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.assignPersonnelUseCase = assignPersonnelUseCase;
        this.updateUserAssignmentUseCase = updateUserAssignmentUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.getFarmContextUseCase = getFarmContextUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
        this.changeUserPasswordUseCase = changeUserPasswordUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        com.agrotrack.domain.model.entities.User user = userDetails.getUser();
        return ResponseEntity.ok(mapper.toUserDto(user));
    }
    
    @GetMapping("/me/context")
    public ResponseEntity<com.agrotrack.application.dto.FarmContextDto> getMyContext(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam UUID farmId) {
        com.agrotrack.domain.model.entities.User user = userDetails.getUser();
        if (user.getRoleForFarm(farmId).isEmpty()) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(getFarmContextUseCase.executeGetFarmContext(user.getIdUser(), farmId));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @jakarta.validation.Valid UpdateProfileDto body) {
        updateProfileUseCase.executeUpdateProfile(userDetails.getUser().getIdUser(), body);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updateMyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @jakarta.validation.Valid UpdatePasswordDto body) {
        changeUserPasswordUseCase.executeChangeUserPassword(userDetails.getUser().getIdUser(), body.getCurrentPassword(), body.getNewPassword());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('ANY_FARM', 'OWNER')")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(getUserByIdUseCase.executeGetUserById(id));
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasPermission(#farmId, 'FOREMAN')")
    public ResponseEntity<List<UserDto>> getPersonnelByFarm(@PathVariable UUID farmId) {
        List<UserDto> users = getPersonnelByFarmUseCase.executeGetPersonnelByFarm(farmId);
        return ResponseEntity.ok(users);
    }

    public static class AssignRequest {
        public String email;
        public String role;
        public List<UUID> farmIds;
    }

    @PostMapping("/assign")
    @PreAuthorize("hasPermission('ANY_FARM', 'OWNER')")
    public ResponseEntity<Void> assignPersonnel(@RequestBody AssignRequest body, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (body.email == null || body.email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        com.agrotrack.domain.model.entities.User caller = userDetails.getUser();
        for (UUID farmId : body.farmIds) {
            java.util.Optional<com.agrotrack.domain.model.enums.UserRole> role = caller.getRoleForFarm(farmId);
            if (role.isEmpty() || role.get() != com.agrotrack.domain.model.enums.UserRole.OWNER) {
                return ResponseEntity.status(403).build();
            }
        }
        assignPersonnelUseCase.executeAssignPersonnel(body.email, body.role, body.farmIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/assignments")
    @PreAuthorize("hasPermission('ANY_FARM', 'OWNER')")
    public ResponseEntity<Void> updateAssignments(@PathVariable UUID id, @RequestBody AssignRequest body, @AuthenticationPrincipal CustomUserDetails userDetails) {
        com.agrotrack.domain.model.entities.User caller = userDetails.getUser();
        for (UUID farmId : body.farmIds) {
            java.util.Optional<com.agrotrack.domain.model.enums.UserRole> role = caller.getRoleForFarm(farmId);
            if (role.isEmpty() || role.get() != com.agrotrack.domain.model.enums.UserRole.OWNER) {
                return ResponseEntity.status(403).build();
            }
        }
        com.agrotrack.domain.model.enums.UserRole newRole = com.agrotrack.domain.model.enums.UserRole.valueOf(body.role);
        updateUserAssignmentUseCase.executeUpdateUserAssignment(id, newRole, body.farmIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('ANY_FARM', 'OWNER')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        deleteUserUseCase.executeDeleteUser(id);
        return ResponseEntity.ok().build();
    }
}
