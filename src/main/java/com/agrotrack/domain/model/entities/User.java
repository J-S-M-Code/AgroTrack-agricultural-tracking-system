package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.UserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Builder
@AllArgsConstructor
public class User {
    @Setter
    @Getter
    private UUID idUser;

    @Getter
    private String name;
    @Getter
    private String lastName;
    @Getter
    private String dni;
    @Getter
    private String phone;
    @Getter
    private String address;
    @Getter
    private String email;

    @Getter
    private Password password;

    @Getter
    private LocalDateTime creationDate;
    @Setter
    @Getter
    private LocalDateTime lastAccess;
    @Getter
    private boolean active;
    @Getter
    private List<FarmAccess> farmAccesses;

    private User(String name, String lastName, String dni, String phone, String address, String email, Password password) {
        this.name = name;
        this.lastName = lastName;
        this.dni = dni;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.password = password;
        this.creationDate = LocalDateTime.now();
        this.lastAccess = LocalDateTime.now();
        this.active = true;
        this.farmAccesses = new ArrayList<>();
    }

    public static User create(String name, String lastName, String dni, String phone, String address, String email, Password password){
        if (name == null || name.isBlank()){
            throw new BusinessRuleViolationsException("El campo Nombre no puede estar vacio.");
        }
        if (lastName == null || lastName.isBlank()){
            throw new BusinessRuleViolationsException("El campo Apellido no puede estar vacio.");
        }
        if (dni == null || dni.isBlank()){
            throw new BusinessRuleViolationsException("El campo DNI no puede estar vacio.");
        }
        if (phone == null || phone.isBlank()){
            throw new BusinessRuleViolationsException("El campo Telefono no puede estar vacio.");
        }
        if (address == null || address.isBlank()){
            throw new BusinessRuleViolationsException("El campo Direccion no puede estar vacio.");
        }
        if (email == null || email.isBlank()){
            throw new BusinessRuleViolationsException("El campo Email no puede estar vacio.");
        }
        return new User(name, lastName, dni, phone, address, email, password);
    }

    public void changeActivation(boolean newStatus) {
        this.active = newStatus;
    }

    public void changePassword(String newPassword) {
        this.password = new Password(newPassword);
    }

    public boolean validatePassword(String password) {
        return this.password.getValue().equals(password);
    }

    public void addOrUpdateFarmAccess(FarmAccess farmAccess) {
        if (this.farmAccesses == null) {
            this.farmAccesses = new ArrayList<>();
        }
        this.farmAccesses.removeIf(fa -> fa.getFarmId().equals(farmAccess.getFarmId()));
        this.farmAccesses.add(farmAccess);
    }

    public void assignFarmAccesses(List<FarmAccess> farmAccesses) {
        this.farmAccesses = new ArrayList<>(farmAccesses);
    }

    public Optional<UserRole> getRoleForFarm(UUID farmId) {
        if (this.farmAccesses == null) return Optional.empty();
        return this.farmAccesses.stream()
                .filter(fa -> fa.getFarmId().equals(farmId))
                .map(FarmAccess::getRole)
                .findFirst();
    }

    public void updateProfile(String name, String lastName, String phone, String address) {
        if (name != null && !name.isBlank()) this.name = name;
        if (lastName != null && !lastName.isBlank()) this.lastName = lastName;
        if (phone != null && !phone.isBlank()) this.phone = phone;
        if (address != null && !address.isBlank()) this.address = address;
    }
}
