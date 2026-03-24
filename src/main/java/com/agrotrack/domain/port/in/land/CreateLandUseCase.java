package com.agrotrack.domain.port.in.land;

import com.agrotrack.domain.model.entities.Land;

public interface CreateLandUseCase {
    Land execute(Land land);
}
