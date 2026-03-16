package com.university.project.legendsofswordandwand.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.auth.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;

  @InjectMocks AuthServiceImpl authService;

  // TC-AS-01
  @Test
  void register_newUser_savesSuccessfully() {

    when(userRepository.existsByUsername("newUser")).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("hashedPassword");

    assertDoesNotThrow(() -> authService.register(new RegisterRequest("newUser", "pass123")));
    verify(userRepository).save(any());
  }

  // TC-AS-02
  @Test
  void register_duplicateUsername_throwsException() {

    when(userRepository.existsByUsername("takenUser")).thenReturn(true);

    assertThrows(
        RuntimeException.class,
        () -> authService.register(new RegisterRequest("takenUser", "pass1234")));

    verify(userRepository, never()).save(any());
  }
}
