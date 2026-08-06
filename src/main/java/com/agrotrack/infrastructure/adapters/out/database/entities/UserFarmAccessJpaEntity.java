package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_farm_access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFarmAccessJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @ManyToOne(fetch = FetchType.EAGER) // Traer de una vez para saber el nombre de la finca
    @JoinColumn(name = "farm_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private FarmJpaEntity farm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
