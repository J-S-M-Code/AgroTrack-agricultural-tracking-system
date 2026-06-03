package com.agrotrack.infrastructure.adapters.out.persistence.mapper;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.infrastructure.adapters.out.persistence.entity.FarmJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FarmMapper {

    // Evitamos el bucle infinito al no mapear 'lots' detallados bidireccionalmente aquí si no es necesario.
    // MapStruct soporta ignorar o mapear campos.
    @Mapping(target = "lots", ignore = true)
    FarmJpaEntity toJpaEntity(Farm farm);

    @Mapping(target = "lots", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "alerts", ignore = true)
    Farm toDomain(FarmJpaEntity farmJpaEntity);

    List<Farm> toDomainList(List<FarmJpaEntity> farmJpaEntities);
}
