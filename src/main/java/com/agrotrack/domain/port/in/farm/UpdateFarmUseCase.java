package com.agrotrack.domain.port.in.farm;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.enums.ProductiveOrientation;
import java.util.UUID;

public interface UpdateFarmUseCase {
    Farm executeUpdateFarm(UUID farmId, String name, String companyName, String cuit, String numberRENAPSA,
                           ProductiveOrientation productiveOrientation, String address, String imageUrl);
}
