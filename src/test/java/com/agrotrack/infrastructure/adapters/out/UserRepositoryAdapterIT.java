package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.UserRole;
import com.agrotrack.infrastructure.adapters.out.database.repositories.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class UserRepositoryAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgis/postgis:15-3.3").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(userJpaRepository, passwordEncoder);
        userJpaRepository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        // Arrange
        User user = User.create("Juan", "Perez", "12345678", "555-1234", "Address", "juan@test.com", new Password("SecurePass1!"));

        // Act
        User saved = adapter.save(user);
        Optional<User> retrieved = adapter.findById(saved.getIdUser());

        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals("juan@test.com", retrieved.get().getEmail());
        assertEquals("Juan", retrieved.get().getName());
        assertEquals(UserRole.WORKER, retrieved.get().getRole());
    }

    @Test
    void testExistsByEmail() {
        // Arrange
        User user = User.create("Ana", "Gomez", "87654321", "555-4321", "Address 2", "ana@test.com", new Password("SecurePass1!"));
        adapter.save(user);

        // Act & Assert
        assertTrue(adapter.existsByEmail("ana@test.com"));
        assertFalse(adapter.existsByEmail("notfound@test.com"));
    }
}
