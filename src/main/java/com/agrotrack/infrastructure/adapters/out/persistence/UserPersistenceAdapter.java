package com.agrotrack.infrastructure.adapters.out.persistence;

import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.port.out.UserRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.persistence.entity.UserJpaEntity;
import com.agrotrack.infrastructure.adapters.out.persistence.mapper.UserMapper;
import com.agrotrack.infrastructure.adapters.out.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserJpaEntity entity = userMapper.toJpaEntity(user);
        UserJpaEntity savedEntity = userRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByDni(String dni) {
        return userRepository.findByDni(dni).map(userMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userMapper.toDomainList(userRepository.findAll());
    }
}
