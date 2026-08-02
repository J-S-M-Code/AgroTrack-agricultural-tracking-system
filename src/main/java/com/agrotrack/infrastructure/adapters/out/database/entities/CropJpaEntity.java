package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.enums.PhenologicalState;
import com.agrotrack.domain.model.enums.TypeCrop;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_active = true")
public class CropJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true;

    @Column(length = 500)
    private String deletionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCrop typeCrop;

    @Column(nullable = false)
    private String species;

    private String variety;

    @Column(nullable = false)
    private LocalDateTime plantingDate;

    @Column(nullable = false)
    private LocalDateTime estimateHarvestDate;

    private LocalDateTime harvestDate; // Puede ser nulo hasta que se coseche

    private double implantedSurface;
    private String renspa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhenologicalState phenologicalState;

    // Relación con Lot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private LotJpaEntity lot;
}