package com.agrotrack.infrastructure.security;

import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepositoryPort;

    public CustomUserDetailsService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));
        return new CustomUserDetails(user);
    }

    public UserDetails loadUserById(UUID id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el id: " + id));
        return new CustomUserDetails(user);
    }
}
