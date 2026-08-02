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

@Service
public class UserService implements RegisterUserUseCase, ChangeUserPasswordUseCase, ChangeUserActivationUseCase, GetPersonnelByFarmUseCase, GetUserByIdUseCase, AssignPersonnelUseCase, UpdateUserAssignmentUseCase, DeleteUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final FarmRepositoryPort farmRepositoryPort;
    private final ApplicationDtoMapper applicationDtoMapper;

    public UserService(UserRepositoryPort userRepositoryPort, FarmRepositoryPort farmRepositoryPort, ApplicationDtoMapper applicationDtoMapper) {
        this.userRepositoryPort = userRepositoryPort;
        this.farmRepositoryPort = farmRepositoryPort;
        this.applicationDtoMapper = applicationDtoMapper;
    }

    @Override
    @Transactional
    public User executeRegisterUser(String name, String lastName, String dni, String phone,
                        String address, String email, String rawPassword, UserRole role) {

        // 1. Validar unicidad (regla de negocio que requiere de la base de datos)
        if (userRepositoryPort.existsByEmail(email)) {
            throw new BusinessRuleViolationsException("Ya existe un usuario registrado con el correo: " + email);
        }

        // 2. Instanciar el Value Object de Password (aquí explotan las validaciones de seguridad de tu regex)
        Password password = new Password(rawPassword);

        // 3. Crear el usuario (su estado 'active' y las fechas nacen solas según tu constructor)
        User newUser = User.create(name, lastName, dni, phone, address, email, password, role);

        // 4. Guardar y retornar con su UUID asignado por el adaptador
        return userRepositoryPort.save(newUser);
    }

    @Override
    @Transactional
    public void executeChangeUserPassword(UUID userId, String currentRawPassword, String newRawPassword) {

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado con ID: " + userId));

        // Validamos la contraseña vieja antes de dejarlo cambiar a la nueva
        if (!user.validatePassword(currentRawPassword)) {
            throw new BusinessRuleViolationsException("La contraseña actual proporcionada es incorrecta.");
        }

        user.changePassword(newRawPassword);
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
        return applicationDtoMapper.toUserDtoList(users);
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
            user.changeRole(UserRole.valueOf(role));
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationsException("Rol inválido: " + role);
        }

        user.assignFarms(farms);
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

        user.changeRole(newRole);
        user.assignFarms(farms);
        userRepositoryPort.save(user);
    }

    @Override
    @Transactional
    public void executeDeleteUser(UUID userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario no encontrado con ID: " + userId));
        
        user.changeActivation(false);
        user.assignFarms(List.of()); // Limpiar fincas
        userRepositoryPort.save(user);
    }
}