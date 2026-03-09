package com.university.project.legendsofswordandwand.usecases;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.university.project.legendsofswordandwand")
public class UseCase1UserRegistrationAndLogin {

  public static void main(String[] args) {
    //
    //    System.setProperty("spring.profiles.active", "demo");
    //
    //    SpringApplication app = new SpringApplication(UseCase1UserRegistrationAndLogin.class);
    //    app.setWebApplicationType(WebApplicationType.NONE);
    //    app.setAdditionalProfiles("demo");
    //    ConfigurableApplicationContext context = app.run(args);
    //
    //    UserService userService = context.getBean(UserService.class);
    //
    //    System.out.println("-----------------Use Case 1: User Registration and
    // Login-----------------");
    //
    //    Scanner scanner = new Scanner(System.in);
    //
    //    System.out.println("\nTesting Registration");
    //
    //    System.out.print("Enter your username for registration: ");
    //    String username = scanner.nextLine();
    //
    //    System.out.print("Enter your password for registration: ");
    //    String password = scanner.nextLine();
    //
    //    if (userService.registerUser(username, password)) {
    //      System.out.println("User successfully registered!");
    //    } else {
    //      System.out.println("User not registered!");
    //    }
    //
    //    System.out.println("\nTesting Login");
    //
    //    System.out.print("Enter your username for login: ");
    //    String loginUsername = scanner.nextLine();
    //
    //    System.out.print("Enter your password for login: ");
    //    String loginPassword = scanner.nextLine();
    //
    //    if (userService.loginUser(loginUsername, loginPassword)) {
    //      System.out.println("User logged in successfully!");
    //    } else {
    //      System.out.println("User not logged in!");
    //    }
    //
    //    System.out.println("\n-----------------End of Demo-----------------\n\n");
    //
    //    context.close();
  }
}
