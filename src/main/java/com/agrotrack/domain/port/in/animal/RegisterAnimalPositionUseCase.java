package com.agrotrack.domain.port.in.animal;

import org.locationtech.jts.geom.Point;

import java.util.UUID;

public interface RegisterAnimalPositionUseCase {
    void execute(UUID animal, Point position);
}
