package com.agrotrack.domain.port.in.lot;

import java.util.UUID;

public interface DeleteSpectralMapUseCase {
    void executeDeleteSpectralMap(UUID mapId);
}
