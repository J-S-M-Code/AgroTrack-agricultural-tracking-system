package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Alert;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
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
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class AlertRepositoryAdapterIT {

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
    private AlertRepositoryAdapter alertRepositoryAdapter;

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
    private Polygon samplePolygon;

    @BeforeEach
    void setUp() {
        User user = User.create("Test", "User", "45678912", "555", "Addr", "alertuser@test.com", new Password("Secret@2026"), UserRole.WORKER);
        savedUser = userRepositoryAdapter.save(user);

        samplePolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(0, 10),
                new Coordinate(10, 10), new Coordinate(10, 0), new Coordinate(0, 0)
        });

        Farm farm = Farm.create("Finca Alert", "Empresa", "30-55555555-5", "REN1",
                ProductiveOrientation.AGRICULTURAL, "Dir", samplePolygon, samplePolygon.getCentroid(), 100.0, "url");
        Farm savedFarm = farmRepositoryAdapter.save(farm);

        Lot lot = Lot.create("Lote Alert", 50.0, SoilType.CLAYEY, LotType.AGREICULTURAL, "Desc", samplePolygon, savedFarm);
        savedLot = lotRepositoryAdapter.save(lot);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testSaveAndFindAlert() {
        Alert alert = Alert.create(
                "Test Alert", AlertType.SYSTEM_AUTOMATIC, Priority.HIGH, RecordType.ALERT,
                "Description IT", LocalDateTime.now(), savedUser, new ArrayList<>(), savedLot, null, null, null, null
        );

        Alert savedAlert = alertRepositoryAdapter.save(alert);
        assertNotNull(savedAlert.getIdAlert());

        entityManager.flush();
        entityManager.clear();

        Optional<Alert> retrieved = alertRepositoryAdapter.findById(savedAlert.getIdAlert());
        assertTrue(retrieved.isPresent());
        assertEquals("Test Alert", retrieved.get().getTitle());
        assertEquals(Priority.HIGH, retrieved.get().getPriority());
        assertEquals(savedLot.getIdLot(), retrieved.get().getRelatedLot().getIdLot());
        assertNotNull(retrieved.get().getAuthor());
    }
}
