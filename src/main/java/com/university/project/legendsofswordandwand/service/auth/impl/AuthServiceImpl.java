package com.university.project.legendsofswordandwand.service.auth.impl;

import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.auth.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link IAuthService}, handling user registration.
 */
@Service
@RequiredArgsConstructor
class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user with the given credentials.
     *
     * <p>The password is encoded using the configured {@link PasswordEncoder} before
     * being persisted.
     *
     * @param request the {@link RegisterRequest} containing the desired username and password
     * @return the newly created and persisted {@link User}
     * @throws RuntimeException if the requested username is already taken
     */
    @Override
    public User register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user =
                User.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .build();

        return userRepository.save(user);
    }
}