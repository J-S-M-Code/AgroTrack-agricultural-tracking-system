package com.agrotrack.domain.port.in.alert;

import java.io.InputStream;
import java.util.UUID;

public interface UploadAlertImageUseCase {
    String executeUploadAlertImage(UUID farmId, String originalFilename, String contentType, long size, InputStream inputStream);
}
