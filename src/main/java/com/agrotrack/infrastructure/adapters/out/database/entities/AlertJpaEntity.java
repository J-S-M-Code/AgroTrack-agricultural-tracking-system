package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.enums.AlertType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.RecordType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType recordType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserJpaEntity author;

    // ¡MAGIA PARA MinIO!
    @ElementCollection
    @CollectionTable(name = "alert_images", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    // Relaciones (todas opcionales, depende de a qué le sacó foto el ingeniero)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private LotJpaEntity relatedLot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id")
    private CropJpaEntity relatedCrop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    private AnimalJpaEntity relatedAnimal;

    // Geometría específica donde se vio el problema
    @Column(columnDefinition = "geometry(Polygon, 4326)")
    private Polygon polygonLimit;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point centroid;
}