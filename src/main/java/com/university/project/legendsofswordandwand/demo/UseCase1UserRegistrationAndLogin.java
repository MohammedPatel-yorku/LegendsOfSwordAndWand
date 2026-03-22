package com.university.project.legendsofswordandwand.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=demo")
class UseCase1UserRegistrationAndLoginTest {

  @Test
  void applicationContextLoads() {
    assertThat(true).isTrue();
  }

  @Test
  void mainMethodDoesNotThrowException() {
    assertThat(() -> UseCase1UserRegistrationAndLogin.main(new String[] {}))
        .doesNotThrowAnyException();
  }

  @Test
  void useCase1IsSpringBootApplication() {
    assertThat(UseCase1UserRegistrationAndLogin.class)
        .hasAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
  }

  @Test
  void scanBasePackagesIsConfigured() {
    SpringBootApplication annotation =
        UseCase1UserRegistrationAndLogin.class.getAnnotation(SpringBootApplication.class);
    assertThat(annotation.scanBasePackages())
        .contains("com.university.project.legendsofswordandwand");
  }

  @Test
  void demoContextCanBeCreated() {
    SpringApplication app = new SpringApplication(UseCase1UserRegistrationAndLogin.class);
    ConfigurableApplicationContext context =
        app.run(new String[] {"--spring.profiles.active=demo"});
    assertThat(context).isNotNull();
    assertThat(context.isActive()).isTrue();
    context.close();
    assertThat(context.isActive()).isFalse();
  }
}
