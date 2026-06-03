package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.enums.LotState;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.SoilType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;

@Entity
@Table(name = "lots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private double hectares;

    @Enumerated(EnumType.STRING)
    private SoilType soilType;

    @Enumerated(EnumType.STRING)
    private LotType type;

    private String description;

    // Magia PostGIS para el perímetro del Lote
    @Column(columnDefinition = "geometry(Polygon, 4326)", nullable = false)
    private Polygon polygonLimit;

    @Enumerated(EnumType.STRING)
    private LotState state;

    // Relación con Farm (Para que en la base de datos haya una FK farm_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id")
    private FarmJpaEntity farm;
}