package com.agrotrack.domain.port.in.lot;

import java.util.UUID;

public interface RevokeLotUseCase {
    void execute(UUID lotId, String reason);
}
