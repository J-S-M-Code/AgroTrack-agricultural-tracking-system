package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.enums.CategoryAnimal;
import com.agrotrack.domain.model.enums.Sex;
import com.agrotrack.domain.model.enums.Species;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "animals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_active = true")
public class AnimalJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true;

    @Column(length = 500)
    private String deletionReason;

    private String visualCaravan;

    @Column(nullable = false)
    private String caravanSenasa;

    private String livestockKey;
    private String numRENSPA;
    private String internalManagementCaravan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Species species;

    private String race;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryAnimal category;

    @Column(nullable = false)
    private LocalDateTime birthdate;

    private double currentWeight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_lot_id")
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private LotJpaEntity assignedLot;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collar_id")
    private IoTCollarJpaEntity collar;

    // Magia de JPA: Maneja la FK "animal_id" en la tabla animal_movements automáticamente
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "animal_id")
    private List<AnimalMovementJpaEntity> movementHistory = new ArrayList<>();

    // Magia de JPA: Maneja la FK "animal_id" en la tabla health_events automáticamente
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "animal_id")
    private List<HealthEventJpaEntity> healthHistory = new ArrayList<>();
}