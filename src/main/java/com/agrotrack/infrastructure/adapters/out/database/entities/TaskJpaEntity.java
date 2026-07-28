package com.agrotrack.infrastructure.adapters.out.database.entities;

import com.agrotrack.domain.model.enums.AccionType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_active = true")
public class TaskJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true;

    @Column(length = 500)
    private String deletionReason;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccionType accionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus taskStatus;

    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creationDate;

    private LocalDateTime completeDate;

    // Relaciones con Usuarios
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserJpaEntity creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_id")
    private UserJpaEntity assigned;

    // Relaciones espaciales (Opcionales dependiendo de la tarea)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id")
    private FarmJpaEntity farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private LotJpaEntity lot;

    // Geometrías para indicar un punto o área específica dentro del lote/finca
    @Column(columnDefinition = "geometry(Polygon, 4326)")
    private Polygon polygonLimit;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point centroid;

    // ¡MAGIA PARA MinIO! Crea una tabla auxiliar automáticamente para guardar las URLs
    @ElementCollection
    @CollectionTable(name = "task_images", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String description;
}