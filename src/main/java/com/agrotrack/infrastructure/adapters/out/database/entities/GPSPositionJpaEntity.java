package com.agrotrack.infrastructure.adapters.out.database.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gps_positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GPSPositionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point coordinate;

    @Column(nullable = false)
    private boolean isOutOfBounds;
}
