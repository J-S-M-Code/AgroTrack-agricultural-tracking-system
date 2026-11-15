package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testCreateProductSuccess() {
        Product product = Product.create("Fertilizante Urea", "SENASA-999", "Nitrogeno", "KG", 1000.0, 15);
        
        assertNotNull(product);
        assertEquals("Fertilizante Urea", product.getName());
        assertEquals("SENASA-999", product.getNumSenasa());
        assertEquals("Nitrogeno", product.getActiveIngredient());
        assertEquals("KG", product.getUnitMeasurement());
        assertEquals(1000.0, product.getStockQuantity());
        assertEquals(15, product.getWaitingTime());
    }

    @Test
    void testCreateProductThrowsExceptionWhenNameIsBlank() {
        assertThrows(BusinessRuleViolationsException.class, () -> 
            Product.create("", "SENASA-999", "Nitrogeno", "KG", 1000.0, 15)
        );
    }

    @Test
    void testAddStock() {
        Product product = Product.create("Fertilizante Urea", "SENASA-999", "Nitrogeno", "KG", 1000.0, 15);
        product.addStock(500.0);
        assertEquals(1500.0, product.getStockQuantity());
    }

    @Test
    void testConsumeStockSuccess() {
        Product product = Product.create("Fertilizante Urea", "SENASA-999", "Nitrogeno", "KG", 1000.0, 15);
        product.consumeStock(400.0);
        assertEquals(600.0, product.getStockQuantity());
    }

    @Test
    void testConsumeStockThrowsExceptionWhenInsufficientStock() {
        Product product = Product.create("Fertilizante Urea", "SENASA-999", "Nitrogeno", "KG", 100.0, 15);
        assertThrows(BusinessRuleViolationsException.class, () -> 
            product.consumeStock(200.0)
        );
    }
}
