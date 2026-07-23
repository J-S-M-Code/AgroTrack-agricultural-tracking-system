package com.agrotrack.domain.port.in.crop;

import java.util.UUID;

public interface DeleteCropUseCase {
    void executeDeleteCrop(UUID cropId, String reason);
}
