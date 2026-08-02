package com.agrotrack.infrastructure.adapters.out.lot;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.ProductiveOrientation;
import com.agrotrack.domain.model.enums.SoilType;
import com.agrotrack.infrastructure.adapters.out.FarmRepositoryAdapter;
import com.agrotrack.infrastructure.adapters.out.database.repositories.FarmJpaRepository;
import com.agrotrack.infrastructure.adapters.out.database.repositories.LotJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
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
class LotRepositoryAdapterIT {

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
    private LotJpaRepository lotJpaRepository;

    @Autowired
    private FarmJpaRepository farmJpaRepository;

    private LotRepositoryAdapter lotAdapter;
    private FarmRepositoryAdapter farmAdapter;

    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);
    private Farm farm;
    private Polygon lotPolygon;

    @BeforeEach
    void setUp() {
        farmAdapter = new FarmRepositoryAdapter(farmJpaRepository);
        lotAdapter = new LotRepositoryAdapter(lotJpaRepository, farmAdapter);

        lotJpaRepository.deleteAll();
        farmJpaRepository.deleteAll();

        Polygon farmPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(0, 20), new Coordinate(20, 20), new Coordinate(20, 0), new Coordinate(0, 0)
        });

        farm = Farm.create("Finca IT", "Corp IT", "30-1111111-1", "REN1", ProductiveOrientation.AGRICULTURAL, "Dir", farmPolygon, farmPolygon.getCentroid(), 100.0, "url");
        farm = farmAdapter.save(farm);

        lotPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(1, 1), new Coordinate(1, 5), new Coordinate(5, 5), new Coordinate(5, 1), new Coordinate(1, 1)
        });
    }

    @Test
    void testSaveAndFindById() {
        // Arrange
        Lot lot = Lot.create("Lote 1", 10.0, SoilType.CLAYEY, LotType.AGRICULTURAL, "Desc", lotPolygon, farm);

        // Act
        Lot saved = lotAdapter.save(lot);
        Optional<Lot> retrieved = lotAdapter.findById(saved.getIdLot());

        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals("Lote 1", retrieved.get().getName());
        assertNotNull(retrieved.get().getPolygonLimit(), "Polygon was not saved correctly");
        assertEquals(farm.getIdFarm(), retrieved.get().getFarm().getIdFarm());
    }

    @Test
    void testExistsOverlappingLot() {
        // Arrange
        Lot lot1 = Lot.create("Lote 1", 10.0, SoilType.CLAYEY, LotType.AGRICULTURAL, "Desc", lotPolygon, farm);
        lotAdapter.save(lot1);

        Polygon overlappingPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(3, 3), new Coordinate(3, 8), new Coordinate(8, 8), new Coordinate(8, 3), new Coordinate(3, 3)
        });

        Polygon notOverlappingPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(10, 10), new Coordinate(10, 15), new Coordinate(15, 15), new Coordinate(15, 10), new Coordinate(10, 10)
        });

        // Act & Assert
        assertTrue(lotAdapter.existsOverlappingLot(overlappingPolygon, null, LotType.AGRICULTURAL), "Debería detectar superposición");
        assertFalse(lotAdapter.existsOverlappingLot(notOverlappingPolygon, null, LotType.AGRICULTURAL), "No debería detectar superposición");
        assertFalse(lotAdapter.existsOverlappingLot(lotPolygon, lot1.getIdLot(), LotType.AGRICULTURAL), "No debería chocar consigo misma");
    }
}
