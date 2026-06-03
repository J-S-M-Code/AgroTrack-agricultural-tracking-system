package com.agrotrack.infrastructure.adapters.out.persistence;

import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.port.out.LotRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.persistence.entity.LotJpaEntity;
import com.agrotrack.infrastructure.adapters.out.persistence.mapper.LotMapper;
import com.agrotrack.infrastructure.adapters.out.persistence.repository.SpringDataLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LotPersistenceAdapter implements LotRepositoryPort {

    private final SpringDataLotRepository lotRepository;
    private final LotMapper lotMapper;

    @Override
    public Lot save(Lot lot) {
        LotJpaEntity entity = lotMapper.toJpaEntity(lot);
        LotJpaEntity savedEntity = lotRepository.save(entity);
        return lotMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Lot> findById(UUID id) {
        return lotRepository.findById(id).map(lotMapper::toDomain);
    }

    @Override
    public List<Lot> findByFarmId(UUID farmId) {
        return lotMapper.toDomainList(lotRepository.findByFarmIdFarm(farmId));
    }
}
