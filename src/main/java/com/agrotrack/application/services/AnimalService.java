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
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AnimalService implements RegisterAnimalUseCase, MoveAnimalUseCase, RegisterHealthEventUseCase {

    private final AnimalRepositoryPort animalRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final AnimalMovementRepositoryPort animalMovementRepositoryPort;
    private final HealthEventRepositoryPort healthEventRepositoryPort;

    public AnimalService(AnimalRepositoryPort animalRepositoryPort, LotRepositoryPort lotRepositoryPort,
                         UserRepositoryPort userRepositoryPort, AnimalMovementRepositoryPort animalMovementRepositoryPort,
                         HealthEventRepositoryPort healthEventRepositoryPort) {
        this.animalRepositoryPort = animalRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.animalMovementRepositoryPort = animalMovementRepositoryPort;
        this.healthEventRepositoryPort = healthEventRepositoryPort;
    }

    @Override
    @Transactional
    public Animal executeRegisterAnimal(String visualCaravan, String caravanSenasa, String livestockKey, String numRENSPA,
                          String internalManagementCaravan, Species species, String race, Sex sex,
                          CategoryAnimal category, LocalDateTime birthdate, double currentWeight, UUID assignedLotId) {

        // 1. Validar que la caravana SENASA no esté repetida
        if (animalRepositoryPort.existsByCaravanSenasa(caravanSenasa)) {
            throw new BusinessRuleViolationsException("Ya existe un animal registrado con la caravana SENASA: " + caravanSenasa);
        }

        // 2. Buscar el lote inicial
        Lot initialLot = lotRepositoryPort.findById(assignedLotId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Lote no encontrado con ID: " + assignedLotId));

        // 3. Crear y guardar el animal
        Animal newAnimal = Animal.create(
                visualCaravan, caravanSenasa, livestockKey, numRENSPA, internalManagementCaravan,
                species, race, sex, category, birthdate, currentWeight, initialLot
        );

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
}