package com.university.project.legendsofswordandwand.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @Test
  void saveUserReturnsSavedUser() {
    User user = new User("username", "password");

    when(userRepository.save(user)).thenReturn(user);

    User savedUser = userService.save(user);

    assertThat(savedUser.getUsername()).isEqualTo("username");
    assertThat(savedUser.getPassword()).isEqualTo("password");
    verify(userRepository).save(user);
  }
}
