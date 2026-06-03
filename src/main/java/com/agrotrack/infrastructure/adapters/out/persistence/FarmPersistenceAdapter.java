package com.agrotrack.infrastructure.adapters.out.persistence;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.port.out.FarmRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.persistence.entity.FarmJpaEntity;
import com.agrotrack.infrastructure.adapters.out.persistence.mapper.FarmMapper;
import com.agrotrack.infrastructure.adapters.out.persistence.repository.SpringDataFarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FarmPersistenceAdapter implements FarmRepositoryPort {

    private final SpringDataFarmRepository farmRepository;
    private final FarmMapper farmMapper;

    @Override
    public Farm save(Farm farm) {
        FarmJpaEntity entity = farmMapper.toJpaEntity(farm);
        FarmJpaEntity savedEntity = farmRepository.save(entity);
        return farmMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Farm> findById(UUID id) {
        return farmRepository.findById(id).map(farmMapper::toDomain);
    }

    @Override
    public List<Farm> findAll() {
        return farmMapper.toDomainList(farmRepository.findAll());
    }

    @Override
    public List<Farm> findByUserId(UUID userId) {
        return farmMapper.toDomainList(farmRepository.findAllByUserId(userId));
    }
}
