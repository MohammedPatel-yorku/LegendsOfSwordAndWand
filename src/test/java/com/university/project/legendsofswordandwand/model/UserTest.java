package com.university.project.legendsofswordandwand.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  void createsUserCorrectly() {
    User user = new User("username", "password");
    assertThat(user.getUsername()).isEqualTo("username");
    assertThat(user.getPassword()).isEqualTo("password");
    assertThat(user.getId()).isNull();
  }
}
