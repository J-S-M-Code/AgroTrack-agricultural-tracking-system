package com.agrotrack.domain.port.out.user;

import com.agrotrack.domain.model.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByDni(String dni);
    List<User> findAll();
    List<User> findByFarmId(UUID farmId);
}