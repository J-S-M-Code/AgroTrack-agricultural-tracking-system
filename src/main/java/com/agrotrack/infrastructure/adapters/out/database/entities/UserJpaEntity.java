package com.agrotrack.infrastructure.adapters.out.database.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String dni;

    private String phone;
    private String address;

    @Column(nullable = false)
    private String email;

    // Aquí guardamos el valor interno del Value Object Password
    @Column(nullable = false)
    private String passwordHash;

    private boolean isActive;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private java.util.List<UserFarmAccessJpaEntity> farmAccesses = new java.util.ArrayList<>();
}