package com.agrotrack.infrastructure.adapters.out.persistence.entity;

import com.agrotrack.domain.model.enums.ProductiveOrientation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "farm")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_farm")
    private UUID idFarm;

    @Column(nullable = false)
    private String name;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String cuit;

    @Column(name = "number_renapsa", nullable = false)
    private String numberRENAPSA;

    @Enumerated(EnumType.STRING)
    @Column(name = "productive_orientation", nullable = false)
    private ProductiveOrientation productiveOrientation;

    @Column(nullable = false)
    private String address;

    @Column(name = "polygon_limit", columnDefinition = "geometry(Polygon,4326)", nullable = false)
    private Polygon polygonLimit;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point centroid;

    @Column(nullable = false)
    private double surface;

    @Column(name = "image_url")
    private String imageUrl;

    // Relaciones diferidas o gestionadas según el caso de uso
    // Por simplicidad, muchas veces en hexagonal, las relaciones (OneToMany) se leen separadamente
    // o se mapean aquí si se necesita carga ansiosa o en cascada.
    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LotJpaEntity> lots;
}
