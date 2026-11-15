package com.agrotrack.application.services;

import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.port.in.crop.GetUnassignedCropsUseCase;
import com.agrotrack.domain.port.out.crop.CropRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetUnassignedCropsService implements GetUnassignedCropsUseCase {

    private final CropRepositoryPort cropRepositoryPort;

    public GetUnassignedCropsService(CropRepositoryPort cropRepositoryPort) {
        this.cropRepositoryPort = cropRepositoryPort;
    }

    @Override
    public List<Crop> executeGetUnassignedCrops(UUID userId) {
        return cropRepositoryPort.findUnassignedCropsByUserId(userId);
    }
}
