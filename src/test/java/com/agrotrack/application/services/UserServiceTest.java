package com.agrotrack.application.services;

import com.agrotrack.application.mapper.ApplicationDtoMapper;
import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.UserRole;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private ApplicationDtoMapper applicationDtoMapper;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void executeRegisterUser_Success() {
        // Arrange
        when(userRepositoryPort.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("EncodedPass94");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setIdUser(UUID.randomUUID());
            return u;
        });

        // Act
        User result = userService.executeRegisterUser("Juan", "Perez", "123", "456", "Dir", "test@test.com", "SecurePass1!", true);

        // Assert
        assertNotNull(result.getIdUser());
        assertEquals("test@test.com", result.getEmail());
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    void executeRegisterUser_ThrowsExceptionWhenEmailExists() {
        // Arrange
        when(userRepositoryPort.existsByEmail("test@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessRuleViolationsException.class, () -> 
            userService.executeRegisterUser("Juan", "Perez", "123", "456", "Dir", "test@test.com", "SecurePass1!", true)
        );
    }

    @Test
    void executeChangeUserActivation_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.create("Juan", "P", "1", "1", "D", "e@e.com", new Password("SecurePass1!"));
        assertTrue(user.isActive());
        
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.executeChangeUserActivation(userId, false);

        // Assert
        assertFalse(user.isActive());
        verify(userRepositoryPort).save(user);
    }
}
