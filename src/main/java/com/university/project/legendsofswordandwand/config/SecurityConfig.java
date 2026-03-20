package com.university.project.legendsofswordandwand.config;

import com.university.project.legendsofswordandwand.service.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// with the help of AI
/**
 * Spring Security configuration for the application.
 *
 * <p>Configures authentication via a {@link CustomUserDetailsService} backed by BCrypt
 * password hashing, defines HTTP request authorization rules, and sets up form-based
 * login and logout behaviour.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Wires the CustomUserDetailsService and BCrypt encoder into Spring Security's authentication
     * provider so it knows how to look up and verify users.
     *
     * @return a configured {@link DaoAuthenticationProvider}
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the {@link AuthenticationManager} as a Spring bean for use in authentication flows.
     *
     * @param authenticationConfiguration the Spring Security authentication configuration
     * @return the application's {@link AuthenticationManager}
     * @throws Exception if the manager cannot be resolved
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures the HTTP security filter chain.
     *
     * <p>Permits unauthenticated access to {@code /login}, {@code /register}, and static
     * assets. All other requests require authentication. Form login redirects to
     * {@code /dashboard} on success. Logout invalidates the session, removes the
     * {@code JSESSIONID} cookie, and redirects to {@code /login?logout}.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the filter chain cannot be built
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**")
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
     * automatically applies salting and adaptive hashing.
     *
     * @return a BCrypt-based {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}