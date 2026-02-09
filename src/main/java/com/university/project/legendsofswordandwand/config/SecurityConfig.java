package com.university.project.legendsofswordandwand.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// with the help of AI
@Configuration
public class SecurityConfig {

  /**
   * makes a PasswordEncoder bean that uses BCrypt hashing BCrypt is a hashing algorithm that
   * automatically applies salting and adaptive hashing
   *
   * @return a BCrypt-based PasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
