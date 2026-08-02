package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.entities.AnimalMovement;
import com.agrotrack.domain.model.entities.HealthEvent;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.CategoryAnimal;
import com.agrotrack.domain.model.enums.HealthEventType;
import com.agrotrack.domain.model.enums.Sex;
import com.agrotrack.domain.model.enums.Species;
import com.agrotrack.domain.port.in.animal.MoveAnimalUseCase;
import com.agrotrack.domain.port.in.animal.RegisterAnimalUseCase;
import com.agrotrack.domain.port.in.animal.RegisterHealthEventUseCase;
import com.agrotrack.domain.port.out.animal.AnimalMovementRepositoryPort;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import com.agrotrack.domain.port.out.animal.HealthEventRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.domain.port.in.animal.GetAnimalsByFarmUseCase;
import com.agrotrack.domain.port.in.animal.GetAnimalByIdUseCase;
import com.agrotrack.domain.port.in.animal.UpdateAnimalUseCase;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.agrotrack.domain.port.in.animal.DeleteAnimalUseCase;

@Service
public class AnimalService implements RegisterAnimalUseCase, MoveAnimalUseCase, RegisterHealthEventUseCase, GetAnimalsByFarmUseCase, GetAnimalByIdUseCase, UpdateAnimalUseCase, DeleteAnimalUseCase {

    private final AnimalRepositoryPort animalRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final AnimalMovementRepositoryPort animalMovementRepositoryPort;
    private final HealthEventRepositoryPort healthEventRepositoryPort;
    private final com.agrotrack.domain.port.out.iot.IoTCollarRepositoryPort iotCollarRepositoryPort;

    public AnimalService(AnimalRepositoryPort animalRepositoryPort, LotRepositoryPort lotRepositoryPort,
                         UserRepositoryPort userRepositoryPort, AnimalMovementRepositoryPort animalMovementRepositoryPort,
                         HealthEventRepositoryPort healthEventRepositoryPort, com.agrotrack.domain.port.out.iot.IoTCollarRepositoryPort iotCollarRepositoryPort) {
        this.animalRepositoryPort = animalRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.animalMovementRepositoryPort = animalMovementRepositoryPort;
        this.healthEventRepositoryPort = healthEventRepositoryPort;
        this.iotCollarRepositoryPort = iotCollarRepositoryPort;
    }

    @Override
    @Transactional
    public Animal executeRegisterAnimal(String visualCaravan, String caravanSenasa, String livestockKey, String numRENSPA,
                          String internalManagementCaravan, Species species, String race, Sex sex,
                          CategoryAnimal category, LocalDateTime birthdate, double currentWeight, UUID assignedLotId, UUID assignedCollarId) {

        // 1. Validar que la caravana SENASA no esté repetida
        if (animalRepositoryPort.existsByCaravanSenasa(caravanSenasa)) {
            throw new BusinessRuleViolationsException("Ya existe un animal registrado con la caravana SENASA: " + caravanSenasa);
        }

        // 2. Buscar el lote inicial
        Lot initialLot = null;
        if (assignedLotId != null) {
            initialLot = lotRepositoryPort.findById(assignedLotId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Lote no encontrado con ID: " + assignedLotId));
        }

        // 3. Crear el animal
        Animal newAnimal = Animal.create(
                visualCaravan, caravanSenasa, livestockKey, numRENSPA, internalManagementCaravan,
                species, race, sex, category, birthdate, currentWeight, initialLot
        );
        
        if (assignedCollarId != null) {
            com.agrotrack.domain.model.entities.IoTCollar collar = iotCollarRepositoryPort.findById(assignedCollarId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Collar no encontrado"));
            newAnimal.assignCollar(collar);
            iotCollarRepositoryPort.save(collar);
        }

        return animalRepositoryPort.save(newAnimal);
    }

    @Override
    @Transactional
    public void executeMoveAnimal(UUID animalId, UUID destinationLotId, LocalDateTime entryDate, UUID registeredByUserId) {
        // 1. Buscar las 3 entidades necesarias
        Animal animal = animalRepositoryPort.findById(animalId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Animal no encontrado"));
        Lot destinationLot = lotRepositoryPort.findById(destinationLotId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Lote de destino no encontrado"));
        User user = userRepositoryPort.findById(registeredByUserId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario registrador no encontrado"));

        // 2. Cerrar el movimiento anterior si el animal ya estaba en un lote
        animalMovementRepositoryPort.findActiveMovementByAnimalId(animalId)
                .ifPresent(activeMovement -> {
                    // La fecha de entrada al nuevo lote equivale a la fecha de salida del anterior
                    activeMovement.closeMovement(entryDate);
                    animalMovementRepositoryPort.save(activeMovement);
                });

        // 3. Crear el nuevo movimiento
        AnimalMovement newMovement = AnimalMovement.create(destinationLot, entryDate, user);

        // 4. Actualizar el animal con su nuevo lote y agregar el movimiento al historial
        animal.moveToLot(destinationLot, newMovement);

        // 5. Guardar los cambios
        animalMovementRepositoryPort.save(newMovement);
        animalRepositoryPort.save(animal);
    }

    @Override
    @Transactional
    public void executeRegisterHealthEvent(UUID animalId, LocalDateTime date, HealthEventType type, String treatment,
                        String numAct, String observation, UUID veterinarianId) {

        Animal animal = animalRepositoryPort.findById(animalId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Animal no encontrado"));
        User veterinarian = userRepositoryPort.findById(veterinarianId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Veterinario no encontrado"));

        // Creamos el evento de salud
        HealthEvent healthEvent = HealthEvent.create(date, type, treatment, numAct, observation, veterinarian);

        // Lo agregamos al historial del animal y lo guardamos
        animal.getHealthHistory().add(healthEvent);

        healthEventRepositoryPort.save(healthEvent);
        animalRepositoryPort.save(animal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Animal> executeGetAnimalsByFarm(UUID farmId) {
        return animalRepositoryPort.findByFarmId(farmId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Animal> executeGetAnimalById(UUID animalId) {
        return animalRepositoryPort.findById(animalId);
    }

    @Override
    @Transactional
    public Animal executeUpdateAnimal(UUID id, String visualCaravan, String caravanSenasa, String livestockKey, String numRENSPA,
                               String internalManagementCaravan, Species species, String race, Sex sex,
                               CategoryAnimal category, LocalDateTime birthdate, Double currentWeight, UUID assignedLotId, UUID assignedCollarId) {
        
        Animal animal = animalRepositoryPort.findById(id)
                .orElseThrow(() -> new BusinessRuleViolationsException("Animal no encontrado"));
                
        // Validar si la caravana cambió y si ya existe
        if (!animal.getCaravanSenasa().equals(caravanSenasa) && animalRepositoryPort.existsByCaravanSenasa(caravanSenasa)) {
            throw new BusinessRuleViolationsException("Ya existe un animal registrado con la caravana SENASA: " + caravanSenasa);
        }

        Lot assignedLot = null;
        if (assignedLotId != null) {
            assignedLot = lotRepositoryPort.findById(assignedLotId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Lote no encontrado"));
        }

        com.agrotrack.domain.model.entities.IoTCollar oldCollar = animal.getCollar();

        animal.update(visualCaravan, caravanSenasa, livestockKey, numRENSPA, internalManagementCaravan, species, race, sex, category, birthdate, currentWeight, assignedLot);

        if (assignedCollarId != null) {
            com.agrotrack.domain.model.entities.IoTCollar collar = iotCollarRepositoryPort.findById(assignedCollarId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Collar no encontrado"));
            animal.assignCollar(collar);
            iotCollarRepositoryPort.save(collar);
        } else {
            animal.assignCollar(null);
        }
        
        if (oldCollar != null && (assignedCollarId == null || !oldCollar.getIdCollar().equals(assignedCollarId))) {
            iotCollarRepositoryPort.save(oldCollar);
        }

        return animalRepositoryPort.save(animal);
    }

    @Override
    @Transactional
    public void executeDeleteAnimal(UUID animalId, String reason) {
        Animal animal = animalRepositoryPort.findById(animalId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Animal no encontrado"));
        
        animal.setActive(false);
        animal.setDeletionReason(reason);
        
        // Desvincular collar si lo tiene
        if (animal.getCollar() != null) {
            com.agrotrack.domain.model.entities.IoTCollar collar = animal.getCollar();
            animal.assignCollar(null);
            iotCollarRepositoryPort.save(collar);
        }
        
        // Cerrar movimiento activo
        animalMovementRepositoryPort.findActiveMovementByAnimalId(animalId)
                .ifPresent(activeMovement -> {
                    activeMovement.closeMovement(LocalDateTime.now());
                    animalMovementRepositoryPort.save(activeMovement);
                });

        animalRepositoryPort.save(animal);
    }
}