package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.Task;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.enums.*;
import com.agrotrack.infrastructure.adapters.out.lot.LotRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class TaskRepositoryAdapterIT {

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
    private TaskRepositoryAdapter taskRepositoryAdapter;

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private FarmRepositoryAdapter farmRepositoryAdapter;

    @Autowired
    private LotRepositoryAdapter lotRepositoryAdapter;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private User savedUser;
    private Lot savedLot;
    private Farm savedFarm;

    @BeforeEach
    void setUp() {
        User user = User.create("Test", "User", "45678912", "555", "Addr", "taskuser@test.com", new Password("Secret@2026"), UserRole.WORKER);
        savedUser = userRepositoryAdapter.save(user);

        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(0, 10),
                new Coordinate(10, 10), new Coordinate(10, 0), new Coordinate(0, 0)
        });

        Farm farm = Farm.create("Finca Task", "Empresa", "30-55555555-5", "REN1",
                ProductiveOrientation.AGRICULTURAL, "Dir", polygon, polygon.getCentroid(), 100.0, "url");
        savedFarm = farmRepositoryAdapter.save(farm);

        Lot lot = Lot.create("Lote Task", 50.0, SoilType.CLAYEY, LotType.AGRICULTURAL, "Desc", polygon, savedFarm);
        savedLot = lotRepositoryAdapter.save(lot);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testSaveAndFindTask() {
        Task task = Task.create(
                "Test Task", AccionType.MAINTENANCE, TaskStatus.CREATED,
                LocalDateTime.now().plusDays(1), Priority.MEDIUM,
                LocalDateTime.now(), null,
                savedUser, savedUser, savedFarm, savedLot, null, null, new java.util.ArrayList<>(), "Desc"
        );

        Task savedTask = taskRepositoryAdapter.save(task);
        assertNotNull(savedTask.getIdTask());

        entityManager.flush();
        entityManager.clear();

        Optional<Task> retrieved = taskRepositoryAdapter.findById(savedTask.getIdTask());
        assertTrue(retrieved.isPresent());
        assertEquals("Test Task", retrieved.get().getTitle());
        assertEquals(Priority.MEDIUM, retrieved.get().getPriority());
        assertEquals(savedLot.getIdLot(), retrieved.get().getRelatedLot().getIdLot());
        assertNotNull(retrieved.get().getCreator());
    }
}
