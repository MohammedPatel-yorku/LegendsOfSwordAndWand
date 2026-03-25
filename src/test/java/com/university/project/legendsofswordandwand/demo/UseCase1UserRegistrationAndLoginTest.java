package com.university.project.legendsofswordandwand.demo;

import static org.assertj.core.api.Assertions.*;

import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.dto.response.DashboardInfo;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.auth.IAuthService;
import com.university.project.legendsofswordandwand.service.user.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=demo")
class UseCase1UserRegistrationAndLoginTest {

  @Autowired private IAuthService authService;

  @Autowired private IUserService userService;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    System.out.println("\n--- Setting up test environment ---");
  }

  @Test
  void userCanRegisterSuccessfully() {
    System.out.println("\n[DEMO 1] Testing: userCanRegisterSuccessfully");
    RegisterRequest request = new RegisterRequest("testUser", "password123");


    User registeredUser = authService.register(request);

    System.out.println(" User registered successfully!");
    System.out.println("  - Username: " + registeredUser.getUsername());
    System.out.println("  - User ID: " + registeredUser.getId());

    assertThat(registeredUser).isNotNull();
    assertThat(registeredUser.getUsername()).isEqualTo("testUser");
    assertThat(registeredUser.getId()).isNotNull();
  }

  @Test
  void passwordIsEncodedAfterRegistration() {
    System.out.println("\n[DEMO 2] Testing: passwordIsEncodedAfterRegistration");
    RegisterRequest request = new RegisterRequest("encodedUser", "myPassword");


    User registeredUser = authService.register(request);

    boolean passwordMatches = passwordEncoder.matches("myPassword", registeredUser.getPassword());
    System.out.println(" Password encoded correctly: " + passwordMatches);
    System.out.println("  - Password matches original: " + passwordMatches);
    System.out.println("  - Password is not stored as plaintext: " + !registeredUser.getPassword().equals("myPassword"));

    assertThat(passwordEncoder.matches("myPassword", registeredUser.getPassword())).isTrue();
    assertThat(registeredUser.getPassword()).isNotEqualTo("myPassword");
  }

  @Test
  void duplicateUsernameThrowsException() {
    System.out.println("\n[DEMO 3] Testing: duplicateUsernameThrowsException");
    RegisterRequest request1 = new RegisterRequest("duplicateUser", "password1");
    RegisterRequest request2 = new RegisterRequest("duplicateUser", "password2");


    authService.register(request1);
    System.out.println(" First user registered successfully");

    try {
      authService.register(request2);
      System.out.println(" ERROR: Should have thrown an exception!");
      fail("Expected RuntimeException for duplicate username");
    } catch (RuntimeException e) {
      System.out.println(" Correctly rejected duplicate username!");
      System.out.println("  - Error message: " + e.getMessage());

      assertThatThrownBy(() -> authService.register(request2))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Username already exists");
    }
  }

  @Test
  void userCanBeFoundAfterRegistration() {
    System.out.println("\n[DEMO 4] Testing: userCanBeFoundAfterRegistration");
    RegisterRequest request = new RegisterRequest("findableUser", "password123");
    authService.register(request);


    var foundUser = userRepository.findByUsername("findableUser");

    System.out.println(" User found in repository: " + foundUser.isPresent());
    if (foundUser.isPresent()) {
      System.out.println("  - Found username: " + foundUser.get().getUsername());
      System.out.println("  - Username matches: " + foundUser.get().getUsername().equals("findableUser"));
    }

    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getUsername()).isEqualTo("findableUser");
  }

  @Test
  void userHasZeroWinsAndLossesAfterRegistration() {
    System.out.println("\n[DEMO 5] Testing: userHasZeroWinsAndLossesAfterRegistration");
    RegisterRequest request = new RegisterRequest("newUser", "password123");


    User registeredUser = authService.register(request);

    System.out.println(" New user stats:");
    System.out.println("  - PvP Wins: " + registeredUser.getPvpWins());
    System.out.println("  - PvP Losses: " + registeredUser.getPvpLosses());
    System.out.println("  - Both are zero: " + (registeredUser.getPvpWins() == 0 && registeredUser.getPvpLosses() == 0));

    assertThat(registeredUser.getPvpWins()).isZero();
    assertThat(registeredUser.getPvpLosses()).isZero();
  }

  @Test
  void userIdCanBeRetrievedByUsername() {
    System.out.println("\n[DEMO 6] Testing: userIdCanBeRetrievedByUsername");
    RegisterRequest request = new RegisterRequest("userWithId", "password123");
    User registeredUser = authService.register(request);


    Long retrievedId = userService.getUserIdByUsername("userWithId");

    System.out.println(" User ID retrieved: " + retrievedId);
    System.out.println("  - ID matches registered user: " + retrievedId.equals(registeredUser.getId()));

    assertThat(retrievedId).isEqualTo(registeredUser.getId());
  }

  @Test
  void userIdRetrievalThrowsExceptionForNonExistentUser() {
    System.out.println("\n[DEMO 7] Testing: userIdRetrievalThrowsExceptionForNonExistentUser");

    try {
      userService.getUserIdByUsername("nonexistent");
      System.out.println(" ERROR: Should have thrown an exception!");
      fail("Expected RuntimeException for non-existent user");
    } catch (RuntimeException e) {
      System.out.println(" Correctly threw exception for non-existent user!");
      System.out.println("  - Error message: " + e.getMessage());

      assertThatThrownBy(() -> userService.getUserIdByUsername("nonexistent"))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("User not found");
    }
  }

  @Test
  void dashboardInfoCanBeRetrievedForRegisteredUser() {
    System.out.println("\n[DEMO 8] Testing: dashboardInfoCanBeRetrievedForRegisteredUser");
    RegisterRequest request = new RegisterRequest("dashboardUser", "password123");
    authService.register(request);


    DashboardInfo dashboardInfo = userService.getDashboardInfo("dashboardUser");

    System.out.println(" Dashboard info retrieved!");
    System.out.println("  - Username: " + dashboardInfo.username());
    System.out.println("  - Has Party: " + dashboardInfo.hasParty());
    System.out.println("  - Has Campaign: " + dashboardInfo.hasCampaign());
    System.out.println("  - Dashboard is not null: " + (dashboardInfo != null));

    assertThat(dashboardInfo).isNotNull();
    assertThat(dashboardInfo.username()).isEqualTo("dashboardUser");
    assertThat(dashboardInfo.hasParty()).isFalse();
    assertThat(dashboardInfo.hasCampaign()).isFalse();
  }

  @Test
  void dashboardInfoHasZeroGoldForNewUser() {
    System.out.println("\n[DEMO 9] Testing: dashboardInfoHasZeroGoldForNewUser");
    RegisterRequest request = new RegisterRequest("newDashboardUser", "password123");
    authService.register(request);


    DashboardInfo dashboardInfo = userService.getDashboardInfo("newDashboardUser");

    System.out.println(" New user dashboard stats:");
    System.out.println("  - Gold: " + dashboardInfo.gold());
    System.out.println("  - Party Size: " + dashboardInfo.partySize());
    System.out.println("  - Cumulative Level: " + dashboardInfo.cumulativeLevel());
    System.out.println("  - All are zero: " + (dashboardInfo.gold() == 0 &&
        dashboardInfo.partySize() == 0 && dashboardInfo.cumulativeLevel() == 0));

    assertThat(dashboardInfo.gold()).isZero();
    assertThat(dashboardInfo.partySize()).isZero();
    assertThat(dashboardInfo.cumulativeLevel()).isZero();
  }

  @Test
  void multipleUsersCanRegisterWithDifferentUsernames() {
    System.out.println("\n[DEMO 10] Testing: multipleUsersCanRegisterWithDifferentUsernames");
    RegisterRequest user1 = new RegisterRequest("user1", "pass1");
    RegisterRequest user2 = new RegisterRequest("user2", "pass2");
    RegisterRequest user3 = new RegisterRequest("user3", "pass3");


    User registered1 = authService.register(user1);
    User registered2 = authService.register(user2);
    User registered3 = authService.register(user3);

    System.out.println(" Multiple users registered!");
    System.out.println("  - User1 ID: " + registered1.getId());
    System.out.println("  - User2 ID: " + registered2.getId());
    System.out.println("  - User3 ID: " + registered3.getId());
    System.out.println("  - All IDs are different: " + (registered1.getId() != registered2.getId() &&
        registered2.getId() != registered3.getId()));
    System.out.println("  - Total users in repository: " + userRepository.count());

    assertThat(registered1.getId()).isNotEqualTo(registered2.getId());
    assertThat(registered2.getId()).isNotEqualTo(registered3.getId());
    assertThat(userRepository.count()).isEqualTo(3);
  }

  @Test
  void registeredUserCanLoginWithCorrectPassword() {
    System.out.println("\n[DEMO 11] Testing: registeredUserCanLoginWithCorrectPassword");
    RegisterRequest request = new RegisterRequest("loginUser", "correctPassword");
    User registeredUser = authService.register(request);

    boolean passwordMatches = passwordEncoder.matches("correctPassword", registeredUser.getPassword());

    System.out.println(" Password verification for login:");
    System.out.println("  - Correct password matches: " + passwordMatches);

    assertThat(passwordMatches).isTrue();
  }

  @Test
  void registeredUserCannotLoginWithWrongPassword() {
    System.out.println("\n[DEMO 12] Testing: registeredUserCannotLoginWithWrongPassword");
    RegisterRequest request = new RegisterRequest("secureUser", "correctPassword");
    User registeredUser = authService.register(request);

    boolean passwordMatches = passwordEncoder.matches("wrongPassword", registeredUser.getPassword());

    System.out.println(" Password verification for wrong password:");
    System.out.println("  - Wrong password matches: " + passwordMatches);
    System.out.println("  - Correctly rejects wrong password: " + !passwordMatches);

    assertThat(passwordMatches).isFalse();
  }
}
