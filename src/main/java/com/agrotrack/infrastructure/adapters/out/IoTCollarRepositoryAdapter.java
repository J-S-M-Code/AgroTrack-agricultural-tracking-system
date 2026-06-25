package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.GPSPosition;
import com.agrotrack.domain.model.entities.IoTCollar;
import com.agrotrack.domain.model.enums.State;
import com.agrotrack.domain.port.out.iot.IoTCollarRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.GPSPositionJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.IoTCollarJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.IoTCollarJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class IoTCollarRepositoryAdapter implements IoTCollarRepositoryPort {

    private final IoTCollarJpaRepository repository;

    public IoTCollarRepositoryAdapter(IoTCollarJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public IoTCollar save(IoTCollar collar) {
        IoTCollarJpaEntity entity = new IoTCollarJpaEntity();
        if (collar.getIdCollar() != null) {
            entity.setId(collar.getIdCollar());
        }
        entity.setCodeRFID(collar.getCodeRFID());
        entity.setModel(collar.getModel());
        entity.setState(collar.getState());
        entity.setBatteryLevel(collar.getBatteryLevel());
        
        List<GPSPositionJpaEntity> gpsHistory = collar.getGpsHistory().stream().map(gps -> {
            GPSPositionJpaEntity gpsEntity = new GPSPositionJpaEntity();
            if (gps.getIdGPSPosition() != null) {
                gpsEntity.setId(gps.getIdGPSPosition());
            }
            gpsEntity.setTimestamp(gps.getTimestamp());
            gpsEntity.setCoordinate(gps.getCoordinate());
            gpsEntity.setOutOfBounds(gps.isOutOfBounds());
            return gpsEntity;
        }).collect(Collectors.toList());
        entity.setGpsHistory(gpsHistory);

        IoTCollarJpaEntity saved = repository.save(entity);
        collar.setIdCollar(saved.getId());
        return collar;
    }

    @Override
    public Optional<IoTCollar> findById(UUID collarId) {
        return repository.findById(collarId).map(this::mapToDomain);
    }

    @Override
    public Optional<IoTCollar> findByCodeRFID(String codeRFID) {
        return repository.findByCodeRFID(codeRFID).map(this::mapToDomain);
    }

    @Override
    public List<IoTCollar> findByState(State state) {
        return repository.findByState(state).stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<IoTCollar> findByBatteryLevelLessThan(Double batteryThreshold) {
        return repository.findByBatteryLevelLessThan(batteryThreshold).stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    private IoTCollar mapToDomain(IoTCollarJpaEntity entity) {
        // We use create and then reset properties to avoid constructor logic modifying things wrongly,
        // or just set them up directly
        IoTCollar collar = IoTCollar.create(entity.getCodeRFID(), entity.getModel(), entity.getState(), entity.getBatteryLevel());
        collar.setIdCollar(entity.getId());
        
        if (entity.getGpsHistory() != null) {
            for (GPSPositionJpaEntity gpsJpa : entity.getGpsHistory()) {
                GPSPosition gps = GPSPosition.create(gpsJpa.getTimestamp(), gpsJpa.getCoordinate(), gpsJpa.isOutOfBounds());
                gps.setIdGPSPosition(gpsJpa.getId());
                collar.getGpsHistory().add(gps);
            }
        }
        return collar;
    }
}
