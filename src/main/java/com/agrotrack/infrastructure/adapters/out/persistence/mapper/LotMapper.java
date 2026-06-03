package com.agrotrack.infrastructure.adapters.out.persistence.mapper;

import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.infrastructure.adapters.out.persistence.entity.LotJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LotMapper {

    @Mapping(target = "farm", ignore = true) // Evitar recursión
    LotJpaEntity toJpaEntity(Lot lot);

    @Mapping(target = "farm", ignore = true) // Evitar recursión
    @Mapping(target = "crop", ignore = true)
    @Mapping(target = "animals", ignore = true)
    @Mapping(target = "spectralMaps", ignore = true)
    Lot toDomain(LotJpaEntity lotJpaEntity);

    List<Lot> toDomainList(List<LotJpaEntity> lotJpaEntities);
}
