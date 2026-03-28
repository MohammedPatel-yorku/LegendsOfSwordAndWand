package com.university.project.legendsofswordandwand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Legends of Sword and Wand Spring Boot application.
 *
 * <p>Bootstraps the Spring application context, auto-configures all Spring components, initialises
 * the JPA data source, and starts the embedded web server.
 */
@SpringBootApplication
public class LegendsOfSwordAndWandApplication {

  /**
   * Application main method. Delegates to {@link SpringApplication#run} to start the context.
   *
   * @param args command-line arguments passed to the JVM (not used by this application)
   */
  public static void main(String[] args) {
    SpringApplication.run(LegendsOfSwordAndWandApplication.class, args);
  }
}
