package com.agrotrack.domain.port.in.lot;

import java.util.UUID;

public interface GeneratePresignedUrlUseCase {
    String executeGeneratePresignedUrl(UUID farmId, String fileName);
}
