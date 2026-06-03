package com.agrotrack.infrastructure.adapters.out.persistence.mapper;

import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.infrastructure.adapters.out.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "managedFarms", ignore = true) // Mapear aparte si es necesario
    @Mapping(target = "password", expression = "java(user.getPassword().getValue())")
    UserJpaEntity toJpaEntity(User user);

    @Mapping(target = "managedFarms", ignore = true)
    @Mapping(target = "password", expression = "java(new com.agrotrack.domain.model.entities.Password(userJpaEntity.getPassword()))")
    User toDomain(UserJpaEntity userJpaEntity);

    List<User> toDomainList(List<UserJpaEntity> userJpaEntities);
}
