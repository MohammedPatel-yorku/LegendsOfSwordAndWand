package com.university.project.legendsofswordandwand.demo;

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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=demo")
class UseCase1UserRegistrationAndLoginTest {

  @Autowired
  private IAuthService authService;

  @Autowired
  private IUserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  void userCanRegisterSuccessfully() {
    RegisterRequest request = new RegisterRequest("testUser", "password123");
    
    User registeredUser = authService.register(request);
    
    assertThat(registeredUser).isNotNull();
    assertThat(registeredUser.getUsername()).isEqualTo("testUser");
    assertThat(registeredUser.getId()).isNotNull();
  }

  @Test
  void passwordIsEncodedAfterRegistration() {
    RegisterRequest request = new RegisterRequest("encodedUser", "myPassword");
    
    User registeredUser = authService.register(request);
    
    assertThat(passwordEncoder.matches("myPassword", registeredUser.getPassword())).isTrue();
    assertThat(registeredUser.getPassword()).isNotEqualTo("myPassword");
  }

  @Test
  void duplicateUsernameThrowsException() {
    RegisterRequest request1 = new RegisterRequest("duplicateUser", "password1");
    RegisterRequest request2 = new RegisterRequest("duplicateUser", "password2");
    
    authService.register(request1);
    
    assertThatThrownBy(() -> authService.register(request2))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Username already exists");
  }

  @Test
  void userCanBeFoundAfterRegistration() {
    RegisterRequest request = new RegisterRequest("findableUser", "password123");
    authService.register(request);
    
    var foundUser = userRepository.findByUsername("findableUser");
    
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getUsername()).isEqualTo("findableUser");
  }

  @Test
  void userHasZeroWinsAndLossesAfterRegistration() {
    RegisterRequest request = new RegisterRequest("newUser", "password123");
    
    User registeredUser = authService.register(request);
    
    assertThat(registeredUser.getPvpWins()).isZero();
    assertThat(registeredUser.getPvpLosses()).isZero();
  }

  @Test
  void userIdCanBeRetrievedByUsername() {
    RegisterRequest request = new RegisterRequest("userWithId", "password123");
    User registeredUser = authService.register(request);
    
    Long retrievedId = userService.getUserIdByUsername("userWithId");
    
    assertThat(retrievedId).isEqualTo(registeredUser.getId());
  }

  @Test
  void userIdRetrievalThrowsExceptionForNonExistentUser() {
    assertThatThrownBy(() -> userService.getUserIdByUsername("nonexistent"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  void dashboardInfoCanBeRetrievedForRegisteredUser() {
    RegisterRequest request = new RegisterRequest("dashboardUser", "password123");
    authService.register(request);
    
    DashboardInfo dashboardInfo = userService.getDashboardInfo("dashboardUser");
    
    assertThat(dashboardInfo).isNotNull();
    assertThat(dashboardInfo.username()).isEqualTo("dashboardUser");
    assertThat(dashboardInfo.hasParty()).isFalse();
    assertThat(dashboardInfo.hasCampaign()).isFalse();
  }

  @Test
  void dashboardInfoHasZeroGoldForNewUser() {
    RegisterRequest request = new RegisterRequest("newDashboardUser", "password123");
    authService.register(request);
    
    DashboardInfo dashboardInfo = userService.getDashboardInfo("newDashboardUser");
    
    assertThat(dashboardInfo.gold()).isZero();
    assertThat(dashboardInfo.partySize()).isZero();
    assertThat(dashboardInfo.cumulativeLevel()).isZero();
  }

  @Test
  void multipleUsersCanRegisterWithDifferentUsernames() {
    RegisterRequest user1 = new RegisterRequest("user1", "pass1");
    RegisterRequest user2 = new RegisterRequest("user2", "pass2");
    RegisterRequest user3 = new RegisterRequest("user3", "pass3");
    
    User registered1 = authService.register(user1);
    User registered2 = authService.register(user2);
    User registered3 = authService.register(user3);
    
    assertThat(registered1.getId()).isNotEqualTo(registered2.getId());
    assertThat(registered2.getId()).isNotEqualTo(registered3.getId());
    assertThat(userRepository.count()).isEqualTo(3);
  }

  @Test
  void registeredUserCanLoginWithCorrectPassword() {
    RegisterRequest request = new RegisterRequest("loginUser", "correctPassword");
    User registeredUser = authService.register(request);
    
    boolean passwordMatches = passwordEncoder.matches("correctPassword", registeredUser.getPassword());
    
    assertThat(passwordMatches).isTrue();
  }

  @Test
  void registeredUserCannotLoginWithWrongPassword() {
    RegisterRequest request = new RegisterRequest("secureUser", "correctPassword");
    User registeredUser = authService.register(request);
    
    boolean passwordMatches = passwordEncoder.matches("wrongPassword", registeredUser.getPassword());
    
    assertThat(passwordMatches).isFalse();
  }
}
