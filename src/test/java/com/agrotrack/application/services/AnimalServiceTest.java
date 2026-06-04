package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.*;
import com.agrotrack.domain.model.enums.*;
import com.agrotrack.domain.port.out.animal.AnimalMovementRepositoryPort;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import com.agrotrack.domain.port.out.animal.HealthEventRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepositoryPort animalRepositoryPort;

    @Mock
    private LotRepositoryPort lotRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private AnimalMovementRepositoryPort animalMovementRepositoryPort;

    @Mock
    private HealthEventRepositoryPort healthEventRepositoryPort;

    @InjectMocks
    private AnimalService animalService;

    private Animal animal;
    private Lot initialLot;
    private Lot destinationLot;
    private User user;
    private UUID animalId;
    private UUID initialLotId;
    private UUID destinationLotId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        animalId = UUID.randomUUID();
        initialLotId = UUID.randomUUID();
        destinationLotId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Creamos mocks manuales o usamos la clase real si es fácil
        initialLot = mock(Lot.class);
        destinationLot = mock(Lot.class);
        user = mock(User.class);
        
        animal = mock(Animal.class);
    }

    @Test
    void executeRegisterAnimalSuccess() {
        when(animalRepositoryPort.existsByCaravanSenasa("SENASA-123")).thenReturn(false);
        when(lotRepositoryPort.findById(initialLotId)).thenReturn(Optional.of(initialLot));
        when(animalRepositoryPort.save(any(Animal.class))).thenAnswer(i -> i.getArgument(0));

        Animal newAnimal = animalService.executeRegisterAnimal(
                "VIS-01", "SENASA-123", "KEY-01", "REN-01", "INT-01",
                Species.BOVINE, "Angus", Sex.MALE, CategoryAnimal.BULL,
                LocalDateTime.now().minusYears(2), 500.0, initialLotId
        );

        assertNotNull(newAnimal);
        assertEquals("SENASA-123", newAnimal.getCaravanSenasa());
        verify(animalRepositoryPort).save(any(Animal.class));
    }

    @Test
    void executeRegisterAnimalThrowsExceptionWhenSenasaExists() {
        when(animalRepositoryPort.existsByCaravanSenasa("SENASA-123")).thenReturn(true);

        assertThrows(BusinessRuleViolationsException.class, () ->
                animalService.executeRegisterAnimal(
                        "VIS-01", "SENASA-123", "KEY-01", "REN-01", "INT-01",
                        Species.BOVINE, "Angus", Sex.MALE, CategoryAnimal.BULL,
                        LocalDateTime.now().minusYears(2), 500.0, initialLotId
                )
        );

        verify(animalRepositoryPort, never()).save(any(Animal.class));
    }

    @Test
    void executeRegisterAnimalThrowsExceptionWhenLotNotFound() {
        when(animalRepositoryPort.existsByCaravanSenasa("SENASA-123")).thenReturn(false);
        when(lotRepositoryPort.findById(initialLotId)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleViolationsException.class, () ->
                animalService.executeRegisterAnimal(
                        "VIS-01", "SENASA-123", "KEY-01", "REN-01", "INT-01",
                        Species.BOVINE, "Angus", Sex.MALE, CategoryAnimal.BULL,
                        LocalDateTime.now().minusYears(2), 500.0, initialLotId
                )
        );
    }

    @Test
    void executeMoveAnimalSuccess() {
        LocalDateTime entryDate = LocalDateTime.now();
        when(animalRepositoryPort.findById(animalId)).thenReturn(Optional.of(animal));
        when(lotRepositoryPort.findById(destinationLotId)).thenReturn(Optional.of(destinationLot));
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));

        AnimalMovement activeMovement = mock(AnimalMovement.class);
        when(animalMovementRepositoryPort.findActiveMovementByAnimalId(animalId)).thenReturn(Optional.of(activeMovement));

        animalService.executeMoveAnimal(animalId, destinationLotId, entryDate, userId);

        verify(activeMovement).closeMovement(entryDate);
        verify(animalMovementRepositoryPort).save(activeMovement);
        verify(animal).moveToLot(eq(destinationLot), any(AnimalMovement.class));
        verify(animalMovementRepositoryPort, times(2)).save(any(AnimalMovement.class));
        verify(animalRepositoryPort).save(animal);
    }

    @Test
    void executeMoveAnimalThrowsExceptionWhenAnimalNotFound() {
        when(animalRepositoryPort.findById(animalId)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleViolationsException.class, () ->
                animalService.executeMoveAnimal(animalId, destinationLotId, LocalDateTime.now(), userId)
        );
    }

    @Test
    void executeRegisterHealthEventSuccess() {
        LocalDateTime date = LocalDateTime.now();
        when(animalRepositoryPort.findById(animalId)).thenReturn(Optional.of(animal));
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));

        // Stub animal list
        java.util.List<HealthEvent> events = new java.util.ArrayList<>();
        when(animal.getHealthHistory()).thenReturn(events);

        animalService.executeRegisterHealthEvent(
                animalId, date, HealthEventType.VACCINATION, "Vacuna", "ACT-123", "Observacion", userId
        );

        verify(healthEventRepositoryPort).save(any(HealthEvent.class));
        verify(animalRepositoryPort).save(animal);
        assertEquals(1, events.size());
    }
}
