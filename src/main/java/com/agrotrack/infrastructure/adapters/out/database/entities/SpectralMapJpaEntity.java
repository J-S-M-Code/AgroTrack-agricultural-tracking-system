package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.enums.SpectralMapType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "spectral_maps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpectralMapJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String urlSpectralMap; // Aquí guardaremos la URL de MinIO

    @Column(nullable = false)
    private LocalDateTime flightDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpectralMapType indexType;

    private Double cloudCoverPercentage;
    private Double resolutionGSD;
    private Double meanIndexValue;

    // Relación con Lot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private LotJpaEntity lot;
}