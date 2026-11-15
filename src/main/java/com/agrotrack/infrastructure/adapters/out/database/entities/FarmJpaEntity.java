package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.enums.ProductiveOrientation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.hibernate.annotations.SQLRestriction;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "farms")
@SQLRestriction("is_active = true")
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

    @Column(nullable = false)
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

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "deletion_reason")
    private String deletionReason;

    @Column(name = "deletion_date")
    private LocalDateTime deletionDate;
}