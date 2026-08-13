package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.UserRole;
import com.agrotrack.domain.port.in.user.ChangeUserActivationUseCase;
import com.agrotrack.domain.port.in.user.ChangeUserPasswordUseCase;
import com.agrotrack.domain.port.in.user.RegisterUserUseCase;
import com.agrotrack.domain.port.in.user.GetPersonnelByFarmUseCase;
import com.agrotrack.domain.port.in.user.GetUserByIdUseCase;
import com.agrotrack.domain.port.in.user.AssignPersonnelUseCase;
import com.agrotrack.domain.port.in.user.UpdateUserAssignmentUseCase;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.application.dto.UserDto;
import com.agrotrack.application.mapper.ApplicationDtoMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.agrotrack.domain.port.in.user.DeleteUserUseCase;
import com.agrotrack.domain.port.in.user.UpdateProfileUseCase;
import com.agrotrack.domain.port.in.user.InviteUserUseCase;
import com.agrotrack.domain.port.in.user.AcceptInviteUseCase;
import com.agrotrack.application.dto.CreateUserDto;
import com.agrotrack.infrastructure.adapters.out.persistence.entity.UserInviteTokenJpaEntity;
import com.agrotrack.infrastructure.adapters.out.persistence.repository.UserInviteTokenJpaRepository;
import com.agrotrack.infrastructure.adapters.out.mail.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

import com.agrotrack.domain.port.in.user.RemovePersonnelFromFarmUseCase;

@Service
public class UserService implements RegisterUserUseCase, ChangeUserPasswordUseCase, ChangeUserActivationUseCase, GetPersonnelByFarmUseCase, GetUserByIdUseCase, AssignPersonnelUseCase, UpdateUserAssignmentUseCase, DeleteUserUseCase, InviteUserUseCase, AcceptInviteUseCase, UpdateProfileUseCase, RemovePersonnelFromFarmUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final FarmRepositoryPort farmRepositoryPort;
    private final ApplicationDtoMapper applicationDtoMapper;
    private final UserInviteTokenJpaRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepositoryPort userRepositoryPort, FarmRepositoryPort farmRepositoryPort, ApplicationDtoMapper applicationDtoMapper, UserInviteTokenJpaRepository tokenRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.farmRepositoryPort = farmRepositoryPort;
        this.applicationDtoMapper = applicationDtoMapper;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User executeRegisterUser(String name, String lastName, String dni, String phone, String address, String email, String rawPassword) {

        // 1. Validar unicidad (regla de negocio que requiere de la base de datos)
        if (userRepositoryPort.existsByEmail(email)) {
            throw new BusinessRuleViolationsException("Ya existe un usuario registrado con el correo: " + email);
        }

        // 2. Validar la contraseña cruda, luego instanciar el Value Object con la codificada
        new Password(rawPassword);
        Password password = new Password(passwordEncoder.encode(rawPassword));

        // 3. Crear el usuario (su estado 'active' y las fechas nacen solas según tu constructor)
        User newUser = User.create(name, lastName, dni, phone, address, email, password);

        // 4. Guardar y retornar con su UUID asignado por el adaptador
        return userRepositoryPort.save(newUser);
    }

    @Override
    @Transactional
    public void executeChangeUserPassword(UUID userId, String currentRawPassword, String newRawPassword) {

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado con ID: " + userId));

        // Validamos la contraseña vieja antes de dejarlo cambiar a la nueva
        if (!passwordEncoder.matches(currentRawPassword, user.getPassword().getValue())) {
            throw new BusinessRuleViolationsException("La contraseña actual proporcionada es incorrecta.");
        }

        // Validamos la nueva contraseña antes de codificarla
        new Password(newRawPassword);
        user.changePassword(passwordEncoder.encode(newRawPassword));
        userRepositoryPort.save(user);
    }

    @Override
    @Transactional
    public void executeChangeUserActivation(UUID userId, boolean newStatus) {

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado con ID: " + userId));

        user.changeActivation(newStatus);
        userRepositoryPort.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> executeGetPersonnelByFarm(UUID farmId) {
        List<User> users = userRepositoryPort.findByFarmId(farmId);
        return users.stream().map(user -> {
            UserDto dto = applicationDtoMapper.toUserDto(user);
            user.getRoleForFarm(farmId).ifPresent(dto::setRole);
            return dto;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto executeGetUserById(UUID userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado con ID: " + userId));
        return applicationDtoMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void executeAssignPersonnel(String email, String role, List<UUID> farmIds) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado con el correo: " + email));
        
        List<Farm> farms = farmIds.stream()
                .map(id -> farmRepositoryPort.findById(id).orElseThrow(() -> new BusinessRuleViolationsException("Finca no encontrada con ID: " + id)))
                .toList();

        try {
            UserRole assignedRole = UserRole.valueOf(role);
            farms.forEach(f -> {
                user.addOrUpdateFarmAccess(com.agrotrack.domain.model.entities.FarmAccess.builder()
                        .farmId(f.getIdFarm()).farmName(f.getName()).role(assignedRole).build());
            });
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationsException("Rol inválido: " + role);
        }
        userRepositoryPort.save(user);
    }

    @Override
    @Transactional
    public void executeUpdateUserAssignment(UUID userId, UserRole newRole, List<UUID> farmIds) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado con ID: " + userId));

        List<Farm> farms = farmIds.stream()
                .map(id -> farmRepositoryPort.findById(id).orElseThrow(() -> new BusinessRuleViolationsException("Finca no encontrada con ID: " + id)))
                .toList();

        farms.forEach(f -> {
            user.addOrUpdateFarmAccess(com.agrotrack.domain.model.entities.FarmAccess.builder()
                    .farmId(f.getIdFarm()).farmName(f.getName()).role(newRole).build());
        });
        userRepositoryPort.save(user);
    }

    @Override
    @Transactional
    public void executeDeleteUser(UUID userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado con ID: " + userId));
        
        user.changeActivation(false);
        user.assignFarmAccesses(List.of()); // Limpiar fincas
        userRepositoryPort.save(user);
    }

    @Override
    @Transactional
    public void executeInviteUser(CreateUserDto userDto, UUID farmId) {
        // 1. Validar correo
        if (userRepositoryPort.existsByEmail(userDto.getEmail())) {
            throw new BusinessRuleViolationsException("Ya existe un usuario con el correo: " + userDto.getEmail());
        }

        Farm farm = farmRepositoryPort.findById(farmId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Finca no encontrada"));

        // 2. Crear usuario
        String rawPassword = userDto.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = generateRandomPassword();
        }
        new Password(rawPassword);
        Password password = new Password(passwordEncoder.encode(rawPassword));
        User newUser = User.create(userDto.getName(), userDto.getLastName(), userDto.getDni(), userDto.getPhone(), userDto.getAddress(), userDto.getEmail(), password);
        
        List<com.agrotrack.domain.model.entities.FarmAccess> accesses = List.of(
                com.agrotrack.domain.model.entities.FarmAccess.builder().farmId(farmId).farmName(farm.getName()).role(userDto.getRole()).build()
        );
        newUser.assignFarmAccesses(accesses);
        
        newUser = userRepositoryPort.save(newUser);

        // 3. Si se debe enviar correo, generar token y enviar
        if (userDto.isSendEmail()) {
            String tokenValue = UUID.randomUUID().toString();
            UserInviteTokenJpaEntity tokenEntity = UserInviteTokenJpaEntity.builder()
                    .token(tokenValue)
                    .userId(newUser.getIdUser())
                    .farmId(farmId)
                    .expirationDate(LocalDateTime.now().plusHours(24))
                    .build();
            tokenRepository.save(tokenEntity);
            emailService.sendInviteEmail(newUser.getEmail(), tokenValue, farm.getName());
        }
    }

    @Override
    @Transactional
    public void executeAcceptInvite(String token, String newPassword) {
        UserInviteTokenJpaEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleViolationsException("Token de invitación inválido o no encontrado"));

        if (tokenEntity.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleViolationsException("El token de invitación ha expirado");
        }

        User user = userRepositoryPort.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado"));

        new Password(newPassword);
        user.changePassword(passwordEncoder.encode(newPassword));
        userRepositoryPort.save(user);
        
        tokenRepository.delete(tokenEntity);
    }

    @Override
    @Transactional
    public void executeUpdateProfile(UUID userId, com.agrotrack.application.dto.UpdateProfileDto dto) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado con ID: " + userId));
                
        user.updateProfile(dto.getName(), dto.getLastName(), dto.getPhone(), dto.getAddress());
        userRepositoryPort.save(user);
    }

    @Override
    @Transactional
    public void executeRemovePersonnelFromFarm(UUID farmId, UUID userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado con ID: " + userId));

        List<com.agrotrack.domain.model.entities.FarmAccess> newAccesses = user.getFarmAccesses().stream()
                .filter(a -> !a.getFarmId().equals(farmId))
                .toList();

        user.assignFarmAccesses(newAccesses);
        userRepositoryPort.save(user);
    }

    private String generateRandomPassword() {
        return "Temp" + UUID.randomUUID().toString().substring(0, 8) + "!";
    }
}