package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.entities.GPSPosition;
import com.agrotrack.domain.model.entities.IoTCollar;
import com.agrotrack.domain.model.enums.State;
import com.agrotrack.domain.port.in.iot.RegisterGPSPositionUseCase;
import com.agrotrack.domain.port.in.iot.RegisterIoTCollarUseCase;
import com.agrotrack.domain.port.in.iot.GetCollarsByFarmUseCase;
import com.agrotrack.domain.port.in.iot.DeleteIoTCollarUseCase;
import com.agrotrack.domain.port.in.iot.UpdateCollarBatteryUseCase;
import com.agrotrack.domain.port.in.iot.UpdateCollarStateUseCase;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import com.agrotrack.domain.port.out.iot.IoTCollarRepositoryPort;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
public class IoTCollarService implements RegisterIoTCollarUseCase, RegisterGPSPositionUseCase, UpdateCollarBatteryUseCase, UpdateCollarStateUseCase, GetCollarsByFarmUseCase, DeleteIoTCollarUseCase {

    private final IoTCollarRepositoryPort ioTCollarRepositoryPort;
    private final AnimalRepositoryPort animalRepositoryPort;

    public IoTCollarService(IoTCollarRepositoryPort ioTCollarRepositoryPort, AnimalRepositoryPort animalRepositoryPort) {
        this.ioTCollarRepositoryPort = ioTCollarRepositoryPort;
        this.animalRepositoryPort = animalRepositoryPort;
    }

    @Override
    @Transactional
    public IoTCollar executeRegisterIoTCollar(String codeRFID, String model, State state, Double batteryLevel, UUID farmId) {
        if (ioTCollarRepositoryPort.findByCodeRFID(codeRFID).isPresent()) {
            throw new BusinessRuleViolationsException("Ya existe un collar con el código RFID: " + codeRFID);
        }

        IoTCollar newCollar = IoTCollar.create(codeRFID, model, state, batteryLevel, farmId);
        return ioTCollarRepositoryPort.save(newCollar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IoTCollar> executeGetCollarsByFarm(UUID farmId) {
        return ioTCollarRepositoryPort.findByFarmId(farmId);
    }

    @Override
    @Transactional
    public void executeDeleteIoTCollar(UUID collarId) {
        ioTCollarRepositoryPort.delete(collarId);
    }

    @Override
    @Transactional
    public void executeRegisterGPSPosition(UUID collarId, LocalDateTime timestamp, Point coordinate) {
        IoTCollar collar = ioTCollarRepositoryPort.findById(collarId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Collar IoT no encontrado"));

        Optional<Animal> animalOpt = animalRepositoryPort.findByCollarId(collarId);

        boolean isOutOfBounds = false;

        if (animalOpt.isPresent()) {
            Animal animal = animalOpt.get();
            if (animal.getAssignedLot() != null && animal.getAssignedLot().getPolygonLimit() != null) {
                // Evaluamos matemáticamente si la coordenada está dentro del polígono
                isOutOfBounds = !animal.getAssignedLot().getPolygonLimit().contains(coordinate);
            }
        }

        GPSPosition position = GPSPosition.create(timestamp, coordinate, isOutOfBounds);
        collar.addGpsPosition(position);

        ioTCollarRepositoryPort.save(collar);
    }

    @Override
    @Transactional
    public void executeUpdateCollarBattery(UUID collarId, Double currentBatteryLevel) {
        IoTCollar collar = ioTCollarRepositoryPort.findById(collarId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Collar IoT no encontrado"));

        collar.updateBatteryLevel(currentBatteryLevel);
        ioTCollarRepositoryPort.save(collar);
    }

    @Override
    @Transactional
    public void executeUpdateCollarState(UUID collarId, State newState) {
        IoTCollar collar = ioTCollarRepositoryPort.findById(collarId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Collar IoT no encontrado"));

        collar.changeState(newState);
        ioTCollarRepositoryPort.save(collar);
    }
}
