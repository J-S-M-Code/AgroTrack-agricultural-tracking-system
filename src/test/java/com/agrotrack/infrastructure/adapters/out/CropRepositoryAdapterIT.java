package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.PhenologicalState;
import com.agrotrack.domain.model.enums.ProductiveOrientation;
import com.agrotrack.domain.model.enums.SoilType;
import com.agrotrack.domain.model.enums.TypeCrop;
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
class CropRepositoryAdapterIT {

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
    private CropRepositoryAdapter cropRepositoryAdapter;

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

        Farm farm = Farm.create("Finca Crop", "Empresa", "30-55555555-5", "REN1",
                ProductiveOrientation.AGRICULTURAL, "Dir", polygon, polygon.getCentroid(), 100.0, "url");
        Farm savedFarm = farmRepositoryAdapter.save(farm);

        Lot lot = Lot.create("Lote Crop", 50.0, SoilType.CLAYEY, LotType.AGREICULTURAL, "Desc", polygon, savedFarm);
        savedLot = lotRepositoryAdapter.save(lot);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testSaveAndFindCrop() {
        Crop crop = Crop.create(
                TypeCrop.CEREAL, "Maíz", "DK7220", LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(100), savedLot, 40.0, "REN-123", PhenologicalState.GERMINATION
        );

        Crop savedCrop = cropRepositoryAdapter.save(crop);
        assertNotNull(savedCrop.getIdCrop());

        entityManager.flush();
        entityManager.clear();

        Optional<Crop> retrieved = cropRepositoryAdapter.findById(savedCrop.getIdCrop());
        assertTrue(retrieved.isPresent());
        assertEquals("Maíz", retrieved.get().getSpecies());
        assertEquals(PhenologicalState.GERMINATION, retrieved.get().getPhenologicalState());
        assertNotNull(retrieved.get().getAssignedLot());
    }
}
