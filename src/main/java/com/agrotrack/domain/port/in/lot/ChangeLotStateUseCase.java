package com.agrotrack.domain.port.in.lot;

import com.agrotrack.domain.model.enums.LotState;
import java.util.UUID;

public interface ChangeLotStateUseCase {
    // Recibe el ID del lote a modificar y el nuevo estado a asignar
    void execute(UUID lotId, LotState newState);
}