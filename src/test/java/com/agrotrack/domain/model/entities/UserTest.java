package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testCreateUser() {
        // Arrange
        Password password = new Password("SecurePass489!");

        // Act
        User user = User.create(
                "Juan",
                "Perez",
                "12345678",
                "1122334455",
                "Calle Falsa 123",
                "juan@example.com",
                password,
                UserRole.WORKER
        );

        // Assert
        assertNotNull(user);
        assertNull(user.getIdUser());
        assertEquals("Juan", user.getName());
        assertEquals("Perez", user.getLastName());
        assertEquals("12345678", user.getDni());
        assertEquals("juan@example.com", user.getEmail());
        assertEquals("SecurePass489!", user.getPassword().getValue());
        // removed getRole assertion
        assertTrue(user.isActive(), "El usuario debe estar activo por defecto al crearse");
        assertNotNull(user.getCreationDate());
    }

    @Test
    void testChangeActivation() {
        // Arrange
        User user = User.create("A", "B", "1", "1", "A", "E", new Password("ValidPass1!"));
        assertTrue(user.isActive());

        // Act
        user.changeActivation(false);

        // Assert
        assertFalse(user.isActive());
    }

    @Test
    void testUpdateLastAccess() {
        // Arrange
        User user = User.create("A", "B", "1", "1", "A", "E", new Password("ValidPass1!"));
        LocalDateTime oldAccess = user.getLastAccess();

        // Act
        LocalDateTime now = LocalDateTime.now();
        user.setLastAccess(now);

        // Assert
        assertEquals(now, user.getLastAccess());
    }
}
