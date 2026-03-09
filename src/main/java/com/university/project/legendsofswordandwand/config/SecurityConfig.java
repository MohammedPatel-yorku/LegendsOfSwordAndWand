package com.university.project.legendsofswordandwand.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// with the help of AI
@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/login", "/register", "/css/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form -> form.loginPage("/login").defaultSuccessUrl("/dashboard", true).permitAll())
        .logout(
            logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID"));

    return http.build();
  }

  /**
   * Makes a PasswordEncoder Bean that uses BCrypt hashing. BCrypt is a hashing algorithm that
   * automatically applies salting and adaptive hashing
   *
   * @return a Bcrypt-based PasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
