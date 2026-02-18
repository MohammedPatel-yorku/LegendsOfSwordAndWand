package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserServiceTest {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userService = new UserService(userRepository, passwordEncoder);
  }

  @Test
  @DisplayName("register user successfully")
  void registerUser_Success() {

    when(userRepository.findByUsername("mage")).thenReturn(null);
    when(passwordEncoder.encode("1234")).thenReturn("hashed1234");

    boolean result = userService.registerUser("mage", "1234");

    assertThat(result).isTrue();
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("register fails if username already exists")
  void registerUser_Duplicate() {

    when(userRepository.findByUsername("mage"))
            .thenReturn(new User("mage", "pass"));

    boolean result = userService.registerUser("mage", "1234");

    assertThat(result).isFalse();
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("register fails if invalid input")
  void registerUser_InvalidInput() {

    boolean result = userService.registerUser("", "");

    assertThat(result).isFalse();
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("login success with correct password")
  void loginUser_Success() {

    User user = new User("rogue", "hashed");
    when(userRepository.findByUsername("rogue")).thenReturn(user);
    when(passwordEncoder.matches("1234", "hashed")).thenReturn(true);

    boolean result = userService.loginUser("rogue", "1234");

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("login fails if user not found")
  void loginUser_UserNotFound() {

    when(userRepository.findByUsername("ghost")).thenReturn(null);

    boolean result = userService.loginUser("ghost", "1234");

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("login fails if password incorrect")
  void loginUser_WrongPassword() {

    User user = new User("knight", "hashed");
    when(userRepository.findByUsername("knight")).thenReturn(user);
    when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

    boolean result = userService.loginUser("knight", "wrong");

    assertThat(result).isFalse();
  }
}
