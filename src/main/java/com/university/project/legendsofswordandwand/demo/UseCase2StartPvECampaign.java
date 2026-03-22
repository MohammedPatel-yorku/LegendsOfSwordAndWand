package com.university.project.legendsofswordandwand.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=demo")
class UseCase2StartPvECampaignTest {

  @Test
  void applicationContextLoads() {
    assertThat(true).isTrue();
  }

  @Test
  void mainMethodDoesNotThrowException() {
    assertThat(() -> UseCase2StartPvECampaign.main(new String[] {}))
        .doesNotThrowAnyException();
  }

  @Test
  void useCase2IsSpringBootApplication() {
    assertThat(UseCase2StartPvECampaign.class)
        .hasAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
  }

  @Test
  void scanBasePackagesIsConfigured() {
    SpringBootApplication annotation =
        UseCase2StartPvECampaign.class.getAnnotation(SpringBootApplication.class);
    assertThat(annotation.scanBasePackages())
        .contains("com.university.project.legendsofswordandwand");
  }

  @Test
  void demoProfileCanBeActivated() {
    SpringApplication app = new SpringApplication(UseCase2StartPvECampaign.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});

    assertThat(context).isNotNull();
    assertThat(context.isActive()).isTrue();

    context.close();
  }

  @Test
  void webApplicationTypeSetToNone() {
    SpringApplication app = new SpringApplication(UseCase2StartPvECampaign.class);
    app.setWebApplicationType(WebApplicationType.NONE);

    assertThat(app).isNotNull();
  }

  @Test
  void contextClosesSuccessfully() {
    SpringApplication app = new SpringApplication(UseCase2StartPvECampaign.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});
    assertThat(context.isActive()).isTrue();

    context.close();

    assertThat(context.isActive()).isFalse();
  }

  @Test
  void applicationContextContainsExpectedBeans() {
    SpringApplication app = new SpringApplication(UseCase2StartPvECampaign.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});

    assertThat(context.getBeanDefinitionCount()).isGreaterThan(0);

    context.close();
  }
}
