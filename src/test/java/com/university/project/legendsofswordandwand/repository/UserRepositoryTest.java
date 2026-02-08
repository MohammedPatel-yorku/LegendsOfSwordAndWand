package com.university.project.legendsofswordandwand.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.university.project.legendsofswordandwand.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserRepositoryTest {

  @Autowired private UserRepository userRepository;

  @Test
  void saveAndFindUser() {
    User user = new User("username", "password");
    User savedUser = userRepository.save(user);

    assertThat(savedUser.getId()).isNotNull();

    User found = userRepository.findById(savedUser.getId()).orElseThrow();

    assertThat(found.getUsername()).isEqualTo("username");
    assertThat(found.getPassword()).isEqualTo("password");
  }
}
