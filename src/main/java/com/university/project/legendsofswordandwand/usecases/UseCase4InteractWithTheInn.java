package com.university.project.legendsofswordandwand.usecases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = "com.university.project.legendsofswordandwand")
public class UseCase4InteractWithTheInn {

  public static void main(String[] args) {

    System.setProperty("spring.profiles.active", "demo");

    SpringApplication app = new SpringApplication(UseCase4InteractWithTheInn.class);
    app.setWebApplicationType(WebApplicationType.NONE);
    app.setAdditionalProfiles("demo");
    ConfigurableApplicationContext context = app.run(args);

    context.close();
  }
}
