package com.agrotrack.infrastructure.adapters.out.database.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "animal_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimalMovementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_lot_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private LotJpaEntity originLot;

    @Column(nullable = false)
    private LocalDateTime entryDate;

    private LocalDateTime exitDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_id", nullable = false)
    private UserJpaEntity registeredBy;
}