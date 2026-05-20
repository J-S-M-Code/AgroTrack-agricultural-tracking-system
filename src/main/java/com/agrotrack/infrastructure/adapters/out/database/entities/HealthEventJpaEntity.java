package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.enums.HealthEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "health_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HealthEventType type;

    private String treatment;
    private String numAct;
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id", nullable = false)
    private UserJpaEntity veterinarian;
}