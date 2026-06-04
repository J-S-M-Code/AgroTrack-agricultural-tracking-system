package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
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
class AnimalRepositoryAdapterIT {

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
    private AnimalRepositoryAdapter animalRepositoryAdapter;

    @Autowired
    private FarmRepositoryAdapter farmRepositoryAdapter;

    @Autowired
    private LotRepositoryAdapter lotRepositoryAdapter;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private Lot savedLot;

    @BeforeEach
    void setUp() {
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(0, 10),
                new Coordinate(10, 10), new Coordinate(10, 0), new Coordinate(0, 0)
        });

        Farm farm = Farm.create("Finca Animal", "Empresa", "30-55555555-5", "REN1",
                ProductiveOrientation.AGRICULTURAL, "Dir", polygon, polygon.getCentroid(), 100.0, "url");
        Farm savedFarm = farmRepositoryAdapter.save(farm);

        Lot lot = Lot.create("Lote Animal", 50.0, SoilType.CLAYEY, LotType.AGREICULTURAL, "Desc", polygon, savedFarm);
        savedLot = lotRepositoryAdapter.save(lot);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testSaveAndFindAnimal() {
        Animal animal = Animal.create(
                "VIS-01", "SENASA-IT1", "KEY-01", "REN-01", "INT-01",
                Species.BOVINE, "Angus", Sex.MALE, CategoryAnimal.BULL,
                LocalDateTime.now().minusYears(2), 500.0, savedLot
        );

        Animal savedAnimal = animalRepositoryAdapter.save(animal);
        assertNotNull(savedAnimal.getIdAnimal());

        entityManager.flush();
        entityManager.clear();

        Optional<Animal> retrieved = animalRepositoryAdapter.findById(savedAnimal.getIdAnimal());
        assertTrue(retrieved.isPresent());
        assertEquals("SENASA-IT1", retrieved.get().getCaravanSenasa());
        assertNotNull(retrieved.get().getAssignedLot());
    }

    @Test
    void testExistsByCaravanSenasa() {
        Animal animal = Animal.create(
                "VIS-02", "SENASA-IT2", "KEY-02", "REN-02", "INT-02",
                Species.BOVINE, "Angus", Sex.FEMALE, CategoryAnimal.COW,
                LocalDateTime.now().minusYears(3), 400.0, savedLot
        );
        animalRepositoryAdapter.save(animal);

        assertTrue(animalRepositoryAdapter.existsByCaravanSenasa("SENASA-IT2"));
        assertFalse(animalRepositoryAdapter.existsByCaravanSenasa("SENASA-NO-EXISTE"));
    }
}
