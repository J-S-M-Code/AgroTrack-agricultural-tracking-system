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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userJpaRepository = userJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity;
        if (user.getIdUser() != null) {
            entity = userJpaRepository.findById(user.getIdUser()).orElse(new UserJpaEntity());
        } else {
            entity = new UserJpaEntity();
            entity.setManagedFarms(new java.util.ArrayList<>());
        }
        
        entity.setName(user.getName());
        entity.setLastName(user.getLastName());
        entity.setDni(user.getDni());
        entity.setPhone(user.getPhone());
        entity.setAddress(user.getAddress());
        entity.setEmail(user.getEmail());
        
        String pass = user.getPassword().getValue();
        if (!pass.startsWith("$2a$")) {
            pass = passwordEncoder.encode(pass);
        }
        entity.setPasswordHash(pass);
        entity.setRole(user.getRole());
        entity.setActive(user.isActive());
        
        if (user.getCreationDate() != null) {
            entity.setCreatedAt(user.getCreationDate());
        }
        if (user.getLastAccess() != null) {
            entity.setLastLogin(user.getLastAccess());
        }

        if (user.getManagedFarms() != null) {
            java.util.List<com.agrotrack.infrastructure.adapters.out.database.entities.FarmJpaEntity> farmEntities = user.getManagedFarms().stream().map(farm -> {
                com.agrotrack.infrastructure.adapters.out.database.entities.FarmJpaEntity fe = new com.agrotrack.infrastructure.adapters.out.database.entities.FarmJpaEntity();
                fe.setId(farm.getIdFarm());
                return fe;
            }).collect(java.util.stream.Collectors.toList());
            if (entity.getManagedFarms() != null) {
                entity.getManagedFarms().clear();
                entity.getManagedFarms().addAll(farmEntities);
            } else {
                entity.setManagedFarms(new java.util.ArrayList<>(farmEntities));
            }
        }
        
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
        return userJpaRepository.findByManagedFarms_Id(farmId).stream().map(this::mapToDomain).toList();
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
        
        // Mapear fincas administradas si existen
        if (entity.getManagedFarms() != null && !entity.getManagedFarms().isEmpty()) {
            java.util.List<com.agrotrack.domain.model.entities.Farm> domainFarms = entity.getManagedFarms().stream().map(f -> {
                return com.agrotrack.domain.model.entities.Farm.builder()
                        .idFarm(f.getId())
                        .name(f.getName())
                        .build();
            }).toList();
            user.assignFarms(domainFarms);
        }
        
        return user;
    }
}