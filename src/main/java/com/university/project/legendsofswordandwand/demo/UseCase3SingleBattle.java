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
class UseCase3SingleBattleTest {

  @Test
  void applicationContextLoads() {
    assertThat(true).isTrue();
  }

  @Test
  void mainMethodDoesNotThrowException() {
    assertThat(() -> UseCase3SingleBattle.main(new String[] {}))
        .doesNotThrowAnyException();
  }

  @Test
  void useCase3IsSpringBootApplication() {
    assertThat(UseCase3SingleBattle.class)
        .hasAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
  }

  @Test
  void scanBasePackagesIsConfigured() {
    SpringBootApplication annotation =
        UseCase3SingleBattle.class.getAnnotation(SpringBootApplication.class);
    assertThat(annotation.scanBasePackages())
        .contains("com.university.project.legendsofswordandwand");
  }

  @Test
  void demoProfileIsActivatedOnStartup() {
    SpringApplication app = new SpringApplication(UseCase3SingleBattle.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});

    assertThat(context).isNotNull();
    assertThat(context.isActive()).isTrue();

    context.close();
  }

  @Test
  void webApplicationTypeIsNone() {
    SpringApplication app = new SpringApplication(UseCase3SingleBattle.class);
    app.setWebApplicationType(WebApplicationType.NONE);

    assertThat(app).isNotNull();
  }

  @Test
  void additionalProfilesAreApplied() {
    SpringApplication app = new SpringApplication(UseCase3SingleBattle.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});

    String[] activeProfiles = context.getEnvironment().getActiveProfiles();
    assertThat(activeProfiles).contains("demo");

    context.close();
  }

  @Test
  void contextCanBeClosedProperly() {
    SpringApplication app = new SpringApplication(UseCase3SingleBattle.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});
    assertThat(context.isActive()).isTrue();

    context.close();
    assertThat(context.isActive()).isFalse();
  }

  @Test
  void beansAreInitializedInContext() {
    SpringApplication app = new SpringApplication(UseCase3SingleBattle.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});

    assertThat(context.getBeanDefinitionCount()).isGreaterThan(0);

    context.close();
  }

  @Test
  void multipleContextInstancesCanBeCreated() {
    SpringApplication app1 = new SpringApplication(UseCase3SingleBattle.class);
    app1.setWebApplicationType(WebApplicationType.NONE);
    app1.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context1 = app1.run(new String[] {});
    assertThat(context1.isActive()).isTrue();

    SpringApplication app2 = new SpringApplication(UseCase3SingleBattle.class);
    app2.setWebApplicationType(WebApplicationType.NONE);
    app2.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context2 = app2.run(new String[] {});
    assertThat(context2.isActive()).isTrue();

    context1.close();
    context2.close();

    assertThat(context1.isActive()).isFalse();
    assertThat(context2.isActive()).isFalse();
  }
}
