package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.HealthEventType;
import com.agrotrack.domain.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HealthEventTest {

    private User veterinarian;

    @BeforeEach
    void setUp() {
        veterinarian = User.create("Vet", "Doc", "12345678", "55555", "Clinica", "vet@test.com", new Password("Password987!"));
    }

    @Test
    void testCreateHealthEventSuccess() {
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        
        HealthEvent event = HealthEvent.create(
                date, HealthEventType.VACCINATION, "Vacuna Aftosa", "ACT-001", "Sin reacciones adversas", veterinarian
        );
        
        assertNotNull(event);
        assertEquals(date, event.getDate());
        assertEquals(HealthEventType.VACCINATION, event.getType());
        assertEquals("Vacuna Aftosa", event.getTreatment());
        assertEquals("ACT-001", event.getNumAct());
        assertEquals("Sin reacciones adversas", event.getObservation());
        assertEquals(veterinarian, event.getVeterinarian());
        assertTrue(event.getImages().isEmpty());
    }

    @Test
    void testCreateHealthEventThrowsExceptionWhenFutureDate() {
        assertThrows(BusinessRuleViolationsException.class, () -> 
            HealthEvent.create(
                LocalDateTime.now().plusDays(2), HealthEventType.VACCINATION, "Vacuna Aftosa", "ACT-001", "Obs", veterinarian
            )
        );
    }

    @Test
    void testAddImage() {
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        HealthEvent event = HealthEvent.create(
                date, HealthEventType.VACCINATION, "Vacuna Aftosa", "ACT-001", "Obs", veterinarian
        );
        
        event.addImage("http://images.com/pic1.jpg");
        assertEquals(1, event.getImages().size());
        assertEquals("http://images.com/pic1.jpg", event.getImages().get(0));
    }

    @Test
    void testUpdateObservation() {
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        HealthEvent event = HealthEvent.create(
                date, HealthEventType.VACCINATION, "Vacuna Aftosa", "ACT-001", "Obs", veterinarian
        );
        
        event.updateObservation("Nueva obs");
        assertEquals("Nueva obs", event.getObservation());
    }
}
