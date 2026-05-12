package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.enums.ProductiveOrientation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;

@Entity
@Table(name = "farms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FarmJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String cuit;

    private String numberRENAPSA;

    @Enumerated(EnumType.STRING)
    private ProductiveOrientation productiveOrientation;

    private String address;

    // SRID 4326 es el estándar mundial de GPS (WGS 84).
    // Es el mismo que usará tu frontend
    @Column(columnDefinition = "geometry(Polygon, 4326)")
    private Polygon polygonLimit;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point centroid;

    private double surface;

    private String imageUrl;
}