package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.enums.State;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "iot_collars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IoTCollarJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String codeRFID;

    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;

    @Column(nullable = false)
    private Double batteryLevel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private FarmJpaEntity farm;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "collar_id")
    private List<GPSPositionJpaEntity> gpsHistory = new ArrayList<>();
}
