package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    @Override
    public User save(User user) {
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }
}