package com.agrotrack.domain.port.in.product;

import java.util.UUID;

public interface ManageProductStockUseCase {
    void addStock(UUID productId, double amountToAdd);
    void consumeStock(UUID productId, double amountToConsume);
}