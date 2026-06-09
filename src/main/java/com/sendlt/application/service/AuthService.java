package com.sendlt.application.service;

import com.sendlt.api.dto.auth.AuthResponse;
import com.sendlt.api.dto.auth.LoginRequest;
import com.sendlt.api.dto.auth.RegisterRequest;
import com.sendlt.api.mapper.UserMapper;
import com.sendlt.application.exception.EmailAlreadyExistsException;
import com.sendlt.application.exception.InvalidCredentialsException;
import com.sendlt.application.security.SendltPrincipal;
import com.sendlt.domain.entity.User;
import com.sendlt.domain.enums.UserRole;
import com.sendlt.domain.repository.UserRepository;
import com.sendlt.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.CLIMBER);

        User saved = userRepository.save(user);
        SendltPrincipal principal = new SendltPrincipal(saved);
        return buildAuthResponse(principal, saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));
            SendltPrincipal principal = (SendltPrincipal) authentication.getPrincipal();
            User user = userRepository
                    .findByEmailIgnoreCase(normalizedEmail)
                    .orElseThrow(InvalidCredentialsException::new);
            return buildAuthResponse(principal, user);
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }
    }

    private AuthResponse buildAuthResponse(SendltPrincipal principal, User user) {
        String token = jwtService.generateToken(principal);
        long expiresInSeconds = jwtService.getExpirationMs() / 1000;
        return AuthResponse.of(token, expiresInSeconds, userMapper.toResponse(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
