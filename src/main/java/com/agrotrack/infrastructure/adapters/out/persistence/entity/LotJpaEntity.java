package com.agrotrack.infrastructure.adapters.out.persistence.entity;

import com.agrotrack.domain.model.enums.LotState;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.SoilType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;

@Entity
@Table(name = "lot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_lot")
    private UUID idLot;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double hectares;

    @Enumerated(EnumType.STRING)
    @Column(name = "soil_type", nullable = false)
    private SoilType soilType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LotType type;

    @Column(length = 500)
    private String description;

    @Column(name = "polygon_limit", columnDefinition = "geometry(Polygon,4326)", nullable = false)
    private Polygon polygonLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LotState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private FarmJpaEntity farm;

    // Se pueden agregar Crop, Animals y SpectralMaps después cuando se modelen sus entidades JPA
}
