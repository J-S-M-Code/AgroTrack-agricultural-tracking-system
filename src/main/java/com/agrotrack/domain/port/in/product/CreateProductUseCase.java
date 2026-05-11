package com.agrotrack.domain.port.in.product;

import com.agrotrack.domain.model.entities.Product;

public interface CreateProductUseCase {
    Product execute(String name, String numSenasa, String activeIngredient,
                    String unitMeasurement, double initialStockQuantity, Integer waitingTime);
}