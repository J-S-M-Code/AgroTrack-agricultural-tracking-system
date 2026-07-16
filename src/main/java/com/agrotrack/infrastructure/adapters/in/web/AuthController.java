package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.infrastructure.adapters.in.web.dto.AuthResponse;
import com.agrotrack.infrastructure.adapters.in.web.dto.LoginRequest;
import com.agrotrack.infrastructure.security.JwtProvider;
import jakarta.validation.Valid;
import com.agrotrack.domain.port.in.user.RegisterUserUseCase;
import com.agrotrack.infrastructure.adapters.in.web.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RegisterUserUseCase registerUserUseCase;

    public AuthController(AuthenticationManager authenticationManager, JwtProvider jwtProvider, RegisterUserUseCase registerUserUseCase) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Autenticar al usuario con sus credenciales usando Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Si es válido, lo guardamos en el contexto
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generamos el Token JWT
        String jwt = jwtProvider.generateToken(authentication);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        // Registramos al usuario a través del UseCase (Dominio)
        registerUserUseCase.executeRegisterUser(
                request.getName(),
                request.getLastName(),
                request.getDni(),
                request.getPhone(),
                request.getAddress(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );

        // Una vez registrado, lo autenticamos directamente para devolverle el token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);

        return new ResponseEntity<>(new AuthResponse(jwt), HttpStatus.CREATED);
    }
}
