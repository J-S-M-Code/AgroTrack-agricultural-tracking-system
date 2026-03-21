package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.TypeUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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

    private Password password;

    @Getter
    private TypeUser rol;
    @Getter
    private LocalDateTime creationDate;
    @Setter
    @Getter
    private LocalDateTime lastAccess;
    @Getter
    private boolean active;

    private User(String name, String lastName, String dni, String phone, String address, String email, Password password, TypeUser rol) {
        this.name = name;
        this.lastName = lastName;
        this.dni = dni;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.creationDate = LocalDateTime.now();
        this.lastAccess = LocalDateTime.now();
        this.active = true;
    }

    public static User create(String name, String lastName, String dni, String phone, String address, String email, Password password, TypeUser rol){
        if (name.isBlank() || name == null){
            throw new BusinessRuleViolationsException("El campo Nombre no puede estar vacio.");
        }
        if (lastName.isBlank() || lastName == null){
            throw new BusinessRuleViolationsException("El campo Apellido no puede estar vacio.");
        }
        if (dni.isBlank() || dni == null){
            throw new BusinessRuleViolationsException("El campo DNI no puede estar vacio.");
        }
        if (phone.isBlank() || phone == null){
            throw new BusinessRuleViolationsException("El campo Telefono no puede estar vacio.");
        }
        if (address.isBlank() || address == null){
            throw new BusinessRuleViolationsException("El campo Direccion no puede estar vacio.");
        }
        if (email.isBlank() || email == null){
            throw new BusinessRuleViolationsException("El campo Email no puede estar vacio.");
        }
        return new User(name, lastName, dni, phone, address, email, password, rol);
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
}
