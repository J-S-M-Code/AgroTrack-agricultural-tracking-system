package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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
    private UserRole role;
    @Getter
    private LocalDateTime creationDate;
    @Setter
    @Getter
    private LocalDateTime lastAccess;
    @Getter
    private boolean active;
    @Getter
    private List<Farm> managedFarms;

    private User(String name, String lastName, String dni, String phone, String address, String email, Password password, UserRole role) {
        this.name = name;
        this.lastName = lastName;
        this.dni = dni;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.password = password;
        this.role = role;
        this.creationDate = LocalDateTime.now();
        this.lastAccess = LocalDateTime.now();
        this.active = true;
        this.managedFarms = new ArrayList<>();
    }

    public static User create(String name, String lastName, String dni, String phone, String address, String email, Password password, UserRole role){
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
        return new User(name, lastName, dni, phone, address, email, password, role);
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

    public void addFarm(Farm farm) {
        this.managedFarms.add(farm);
    }
}
