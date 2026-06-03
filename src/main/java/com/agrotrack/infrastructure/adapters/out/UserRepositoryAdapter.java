package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.UserJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getIdUser(),
                user.getName(),
                user.getLastName(),
                user.getDni(),
                user.getPhone(),
                user.getAddress(),
                user.getEmail(),
                user.getPassword().getValue(), // Extraemos el string del Value Object
                user.getRole(),
                user.isActive(),
                user.getCreationDate(),
                user.getLastAccess()
        );

        UserJpaEntity savedEntity = userJpaRepository.save(entity);
        user.setIdUser(savedEntity.getId());
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(this::mapToDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByDni(String dni) {
        // TODO: Implement findByDni in UserJpaRepository
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream().map(this::mapToDomain).toList();
    }

    @Override
    public List<User> findByFarmId(UUID farmId) {
        // TODO: Implement relationship mapping in FarmJpaEntity/UserJpaEntity
        return Collections.emptyList();
    }

    private User mapToDomain(UserJpaEntity entity) {
        // Reconstruimos el Value Object
        Password password = new Password(entity.getPasswordHash());

        User user = User.create(
                entity.getName(), entity.getLastName(), entity.getDni(),
                entity.getPhone(), entity.getAddress(), entity.getEmail(),
                password, entity.getRole()
        );
        user.setIdUser(entity.getId());
        user.changeActivation(entity.isActive()); // Restauramos su estado real
        return user;
    }
}