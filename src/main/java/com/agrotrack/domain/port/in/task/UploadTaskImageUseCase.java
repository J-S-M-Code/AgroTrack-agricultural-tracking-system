package com.agrotrack.domain.port.in.task;

import java.io.InputStream;
import java.util.UUID;

public interface UploadTaskImageUseCase {
    String executeUploadTaskImage(UUID farmId, String originalFilename, String contentType, long size, InputStream inputStream);
}
