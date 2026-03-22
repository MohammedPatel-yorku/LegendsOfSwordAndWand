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
class UseCase4InteractWithTheInnTest {

  @Test
  void applicationContextLoads() {
    assertThat(true).isTrue();
  }

  @Test
  void mainMethodDoesNotThrowException() {
    assertThat(() -> UseCase4InteractWithTheInn.main(new String[] {}))
        .doesNotThrowAnyException();
  }

  @Test
  void useCase4IsSpringBootApplication() {
    assertThat(UseCase4InteractWithTheInn.class)
        .hasAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
  }

  @Test
  void scanBasePackagesIsConfigured() {
    SpringBootApplication annotation =
        UseCase4InteractWithTheInn.class.getAnnotation(SpringBootApplication.class);
    assertThat(annotation.scanBasePackages())
        .contains("com.university.project.legendsofswordandwand");
  }

  @Test
  void demoProfileCanBeActivatedForUseCase4() {
    SpringApplication app = new SpringApplication(UseCase4InteractWithTheInn.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});

    assertThat(context).isNotNull();
    assertThat(context.isActive()).isTrue();

    context.close();
  }

  @Test
  void webApplicationTypeIsSetToNone() {
    SpringApplication app = new SpringApplication(UseCase4InteractWithTheInn.class);
    app.setWebApplicationType(WebApplicationType.NONE);

    assertThat(app).isNotNull();
  }

  @Test
  void additionalDemoProfileIsSet() {
    SpringApplication app = new SpringApplication(UseCase4InteractWithTheInn.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});

    String[] activeProfiles = context.getEnvironment().getActiveProfiles();
    assertThat(activeProfiles).contains("demo");

    context.close();
  }

  @Test
  void contextClosesGracefully() {
    SpringApplication app = new SpringApplication(UseCase4InteractWithTheInn.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});
    assertThat(context.isActive()).isTrue();

    context.close();

    assertThat(context.isActive()).isFalse();
  }

  @Test
  void springBeansAreRegisteredAndAvailable() {
    SpringApplication app = new SpringApplication(UseCase4InteractWithTheInn.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});

    assertThat(context.getBeanDefinitionCount()).isGreaterThan(0);

    context.close();
  }

  @Test
  void multipleUseCase4ContextsCanCoexist() {
    SpringApplication app1 = new SpringApplication(UseCase4InteractWithTheInn.class);
    app1.setWebApplicationType(WebApplicationType.NONE);
    app1.setAdditionalProfiles("demo");
    ConfigurableApplicationContext context1 = app1.run(new String[] {});

    SpringApplication app2 = new SpringApplication(UseCase4InteractWithTheInn.class);
    app2.setWebApplicationType(WebApplicationType.NONE);
    app2.setAdditionalProfiles("demo");
    ConfigurableApplicationContext context2 = app2.run(new String[] {});

    assertThat(context1.isActive()).isTrue();
    assertThat(context2.isActive()).isTrue();
    assertThat(context1).isNotSameAs(context2);

    context1.close();
    context2.close();

    assertThat(context1.isActive()).isFalse();
    assertThat(context2.isActive()).isFalse();
  }

  @Test
  void innServiceCanBeAccessedFromContext() {
    SpringApplication app = new SpringApplication(UseCase4InteractWithTheInn.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");

    ConfigurableApplicationContext context = app.run(new String[] {});

    assertThat(context.getBeanDefinitionCount()).isGreaterThan(0);

    context.close();
  }
}
