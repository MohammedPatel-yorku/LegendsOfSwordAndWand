package com.university.project.legendsofswordandwand.repository;

import com.university.project.legendsofswordandwand.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("have user and find by username")
  void testFindByUsername() {

    User user = new User("wizard", "hashedPassword");
    userRepository.save(user);

    User found = userRepository.findByUsername("wizard");

    assertThat(found).isNotNull();
    assertThat(found.getUsername()).isEqualTo("wizard");
    assertThat(found.getPassword()).isEqualTo("hashedPassword");
  }

  @Test
  @DisplayName("Return null when username not found")
  void testFindByUsername_NotFound() {

    User found = userRepository.findByUsername("ghost");

    assertThat(found).isNull();
  }
}
