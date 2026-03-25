package com.university.project.legendsofswordandwand.demo;

import static org.assertj.core.api.Assertions.*;

import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.auth.IAuthService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=demo")
class UseCase2StartPvECampaignTest {

  @Autowired private ICampaignService campaignService;

  @Autowired private IAuthService authService;

  @Autowired private UserRepository userRepository;

  @Autowired private CampaignRepository campaignRepository;

  @BeforeEach
  void setUp() {
    campaignRepository.deleteAll();
    userRepository.deleteAll();
    System.out.println("\n--- Setting up test environment for USE CASE 2 ---");
  }

  // Case 2-01: Successful Campaign Creation
  @Test
  void testCase2_01_successfulCampaignCreation() {
    System.out.println("\n[Test Case 2-01] Campaign Creation - Successful Flow");
    System.out.println("Category: campaign creation");
    System.out.println("Requirements: UC2 – Successful Campaign Creation");

    System.out.println("\n--- Initial Condition ---");
    RegisterRequest registerRequest = new RegisterRequest("campaignPlayer", "password123");
    var registeredUser = authService.register(registerRequest);
    System.out.println(" System is running");
    System.out.println(" User is registered and logged in");
    System.out.println(" User does not have an active campaign");

    System.out.println("\n--- Procedure ---");
    System.out.println("  User selects 'start new campaign'");
    System.out.println("  System prompts for hero's name and class");
    System.out.println("  User enters valid hero name: 'Aragorn'");
    System.out.println("  User selects valid hero class: 'WARRIOR'");

    Campaign newCampaign =
        campaignService.startNewCampaign("campaignPlayer", "Aragorn", HeroClass.WARRIOR);

    System.out.println("  User confirms creation");

    System.out.println("\n--- Expected Outcome ---");
    System.out.println(" A new campaign is created");
    System.out.println("  - Campaign ID: " + newCampaign.getId());
    assertThat(newCampaign).isNotNull();
    assertThat(newCampaign.getId()).isNotNull();

    System.out.println(" Campaign is marked as active");
    System.out.println("  - Active: " + newCampaign.isActive());
    assertThat(newCampaign.isActive()).isTrue();

    System.out.println(" Campaign starts in room 1");
    System.out.println("  - Current Room: " + newCampaign.getCurrentRoom());
    assertThat(newCampaign.getCurrentRoom()).isEqualTo(1);

    System.out.println(" A new party is created");
    System.out.println("  - Party ID: " + newCampaign.getParty().getId());
    assertThat(newCampaign.getParty()).isNotNull();
    assertThat(newCampaign.getParty().getId()).isNotNull();

    System.out.println(" Starting hero is created with selected name and class");
    System.out.println("  - Party has heroes: " + !newCampaign.getParty().getHeroes().isEmpty());
    assertThat(newCampaign.getParty().getHeroes()).isNotEmpty();

    var startingHero = newCampaign.getParty().getHeroes().stream().findFirst();
    if (startingHero.isPresent()) {
      System.out.println("  - Hero Name: " + startingHero.get().getName());
      System.out.println("  - Hero Class: " + startingHero.get().getHeroClass());
      assertThat(startingHero.get().getName()).isEqualTo("Aragorn");
      assertThat(startingHero.get().getHeroClass()).isEqualTo(HeroClass.WARRIOR);
    }

    System.out.println("\n TEST PASSED: Campaign created successfully");
  }

  // Case 2-02: Campaign Initial State Validation
  @Test
  void testCase2_02_campaignInitialStateValidation() {
    System.out.println("\n[Test Case 2-02] Campaign Initial State Validation");
    System.out.println("Category: campaign initial state validation");
    System.out.println("Requirements: UC2 – campaign initialization rules");

    System.out.println("\n--- Initial Condition ---");
    RegisterRequest registerRequest = new RegisterRequest("stateTestPlayer", "password123");
    authService.register(registerRequest);
    System.out.println(" User is logged in");

    System.out.println("\n--- Procedure ---");
    System.out.println("  User starts a new campaign");
    System.out.println("  System creates the campaign");

    Campaign campaign =
        campaignService.startNewCampaign("stateTestPlayer", "Legolas", HeroClass.MAGE);

    System.out.println("\n--- Expected Outcome ---");
    System.out.println(" Campaign.active = true");
    assertThat(campaign.isActive()).isTrue();
    System.out.println("  - Actual: " + campaign.isActive());

    System.out.println(" Campaign.currentRoom = 1");
    assertThat(campaign.getCurrentRoom()).isEqualTo(1);
    System.out.println("  - Actual: " + campaign.getCurrentRoom());

    System.out.println("\n TEST PASSED: Campaign initial state is valid");
  }

  // Case 2-03: User Validation - Invalid User ID
  @Test
  void testCase2_03_userValidationWithInvalidUser() {
    System.out.println("\n[Test Case 2-03] User Validation");
    System.out.println("Category: user validation");
    System.out.println("Requirements: UC2 – user must exist");

    System.out.println("\n--- Initial Condition ---");
    System.out.println("  System running");
    System.out.println("  Invalid or non-existent user ID");

    System.out.println("\n--- Procedure ---");
    System.out.println("  A campaign start is attempted with an invalid user ID");

    System.out.println("\n--- Expected Outcome ---");
    System.out.println(" System rejects the request");

    try {
      campaignService.startNewCampaign("nonexistentUser", "Hero", HeroClass.WARRIOR);
      System.out.println(" ERROR: Should have thrown an exception!");
      fail("Expected RuntimeException for non-existent user");
    } catch (RuntimeException e) {
      System.out.println(" Error message or exception is generated");
      System.out.println("  - Error: " + e.getMessage());

      System.out.println(" No campaign is created");
      var campaigns = campaignRepository.findAll();
      assertThat(campaigns).isEmpty();

      System.out.println("\n TEST PASSED: User validation works correctly");
    }
  }

  // Case 2-04: Hero Name Validation - Empty Name
  @Test
  void testCase2_04_heroNameValidationEmptyName() {
    System.out.println("\n[Test Case 2-04] Input Validation - Hero Name");
    System.out.println("Category: input validation – hero name");
    System.out.println("Requirements: UC2 – hero name validation");

    System.out.println("\n--- Initial Condition ---");
    RegisterRequest registerRequest = new RegisterRequest("heroNameTestPlayer", "password123");
    authService.register(registerRequest);
    System.out.println(" User logged in");

    System.out.println("\n--- Procedure ---");
    System.out.println("  User selects Start Campaign");
    System.out.println("  User leaves hero name empty");
    System.out.println("  User confirms");

    System.out.println("\n--- Expected Outcome ---");
    try {
      campaignService.startNewCampaign("heroNameTestPlayer", "", HeroClass.WARRIOR);
      System.out.println(" ERROR: Should have thrown an exception!");
      fail("Expected RuntimeException for empty hero name");
    } catch (RuntimeException e) {
      System.out.println(" Campaign creation is rejected");
      System.out.println(" Validation error is displayed");
      System.out.println("  - Error: " + e.getMessage());

      System.out.println(" No campaign is created");
      var campaigns = campaignRepository.findAll();
      assertThat(campaigns).isEmpty();

      System.out.println("\n TEST PASSED: Hero name validation works correctly");
    }
  }

  @Test
  void campaignStartsAtRoom1() {
    System.out.println("\n[DEMO 2] Testing: campaignStartsAtRoom1");
    RegisterRequest registerRequest = new RegisterRequest("roomUser", "password123");
    authService.register(registerRequest);

    Campaign campaign =
        campaignService.startNewCampaign("roomUser", "Legolas", HeroClass.RANGER);

    System.out.println(" Campaign starts at room 1: " + campaign.getCurrentRoom());

    assertThat(campaign.getCurrentRoom()).isEqualTo(1);
  }

  @Test
  void campaignHasPartyWithStartingHero() {
    System.out.println("\n[DEMO 3] Testing: campaignHasPartyWithStartingHero");
    RegisterRequest registerRequest = new RegisterRequest("partyUser", "password123");
    authService.register(registerRequest);

    Campaign campaign =
        campaignService.startNewCampaign("partyUser", "Gimli", HeroClass.WARRIOR);

    System.out.println(" Campaign party information:");
    System.out.println("  - Party exists: " + (campaign.getParty() != null));
    System.out.println("  - Heroes in party: " + campaign.getParty().getHeroes().size());
    System.out.println("  - First hero name: "
        + campaign.getParty().getHeroes().get(0).getName());
    System.out.println("  - First hero class: "
        + campaign.getParty().getHeroes().get(0).getHeroClass());

    assertThat(campaign.getParty()).isNotNull();
    assertThat(campaign.getParty().getHeroes()).isNotEmpty();
  }

  @Test
  void campaignHasOwner() {
    System.out.println("\n[DEMO 4] Testing: campaignHasOwner");
    RegisterRequest registerRequest = new RegisterRequest("ownerUser", "password123");
    authService.register(registerRequest);

    Campaign campaign =
        campaignService.startNewCampaign("ownerUser", "Gandalf", HeroClass.MAGE);

    System.out.println(" Campaign owner information:");
    System.out.println("  - Owner exists: " + (campaign.getOwner() != null));
    System.out.println("  - Owner username: " + campaign.getOwner().getUsername());
    System.out.println("  - Username matches: " + campaign.getOwner().getUsername().equals("ownerUser"));

    assertThat(campaign.getOwner()).isNotNull();
    assertThat(campaign.getOwner().getUsername()).isEqualTo("ownerUser");
  }

  @Test
  void cannotStartCampaignWithoutRegistration() {
    System.out.println("\n[DEMO 5] Testing: cannotStartCampaignWithoutRegistration");

    try {
      campaignService.startNewCampaign("unregisteredUser", "Hero", HeroClass.WARRIOR);
      System.out.println(" ERROR: Should have thrown an exception!");
      fail("Expected RuntimeException for unregistered user");
    } catch (RuntimeException e) {
      System.out.println(" Correctly rejected unregistered user!");
      System.out.println("  - Error message: " + e.getMessage());

      assertThatThrownBy(
              () ->
                  campaignService.startNewCampaign("unregisteredUser", "Hero", HeroClass.WARRIOR))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("User not found");
    }
  }

  @Test
  void cannotStartMultipleCampaignsSametime() {
    System.out.println("\n[DEMO 6] Testing: cannotStartMultipleCampaignsSametime");
    RegisterRequest registerRequest = new RegisterRequest("multiCampaignUser", "password123");
    authService.register(registerRequest);

    campaignService.startNewCampaign("multiCampaignUser", "FirstHero", HeroClass.WARRIOR);
    System.out.println(" First campaign started successfully");

    try {
      campaignService.startNewCampaign("multiCampaignUser", "SecondHero", HeroClass.MAGE);
      System.out.println(" ERROR: Should have thrown an exception!");
      fail("Expected RuntimeException for multiple campaigns");
    } catch (RuntimeException e) {
      System.out.println(" Correctly rejected second campaign!");
      System.out.println("  - Error message: " + e.getMessage());

      assertThatThrownBy(
              () ->
                  campaignService.startNewCampaign(
                      "multiCampaignUser", "SecondHero", HeroClass.MAGE))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Campaign already in progress");
    }
  }

  @Test
  void activeCampaignCanBeRetrieved() {
    System.out.println("\n[DEMO 7] Testing: activeCampaignCanBeRetrieved");
    RegisterRequest registerRequest = new RegisterRequest("retrieveUser", "password123");
    authService.register(registerRequest);

    Campaign startedCampaign =
        campaignService.startNewCampaign("retrieveUser", "Hero", HeroClass.WARRIOR);
    Campaign retrievedCampaign = campaignService.getActiveCampaign("retrieveUser");

    System.out.println(" Campaign retrieval:");
    System.out.println("  - Started campaign ID: " + startedCampaign.getId());
    System.out.println("  - Retrieved campaign ID: " + retrievedCampaign.getId());
    System.out.println("  - IDs match: " + retrievedCampaign.getId().equals(startedCampaign.getId()));

    assertThat(retrievedCampaign.getId()).isEqualTo(startedCampaign.getId());
  }

  @Test
  void userHasActiveCampaignAfterStart() {
    System.out.println("\n[DEMO 8] Testing: userHasActiveCampaignAfterStart");
    RegisterRequest registerRequest = new RegisterRequest("activeCampaignUser", "password123");
    var user = authService.register(registerRequest);

    System.out.println(" Before campaign start: " + campaignService.hasActiveCampaign(user.getId()));
    assertThat(campaignService.hasActiveCampaign(user.getId())).isFalse();

    campaignService.startNewCampaign("activeCampaignUser", "Hero", HeroClass.WARRIOR);

    System.out.println(" After campaign start: " + campaignService.hasActiveCampaign(user.getId()));
    assertThat(campaignService.hasActiveCampaign(user.getId())).isTrue();
  }

  @Test
  void differentUsersCanStartCampaignsSeparately() {
    System.out.println("\n[DEMO 9] Testing: differentUsersCanStartCampaignsSeparately");
    RegisterRequest user1 = new RegisterRequest("user1", "pass1");
    RegisterRequest user2 = new RegisterRequest("user2", "pass2");
    authService.register(user1);
    authService.register(user2);

    Campaign campaign1 =
        campaignService.startNewCampaign("user1", "Hero1", HeroClass.WARRIOR);
    Campaign campaign2 =
        campaignService.startNewCampaign("user2", "Hero2", HeroClass.MAGE);

    System.out.println(" Separate campaigns for different users:");
    System.out.println("  - Campaign1 ID: " + campaign1.getId());
    System.out.println("  - Campaign2 ID: " + campaign2.getId());
    System.out.println("  - Campaign1 Owner: " + campaign1.getOwner().getUsername());
    System.out.println("  - Campaign2 Owner: " + campaign2.getOwner().getUsername());
    System.out.println("  - IDs are different: " + !campaign1.getId().equals(campaign2.getId()));
    System.out.println("  - Owners are different: " +
        !campaign1.getOwner().getUsername().equals(campaign2.getOwner().getUsername()));

    assertThat(campaign1.getId()).isNotEqualTo(campaign2.getId());
    assertThat(campaign1.getOwner().getUsername()).isNotEqualTo(campaign2.getOwner().getUsername());
  }

  @Test
  void partyCumulativeLevelCanBeRetrieved() {
    System.out.println("\n[DEMO 10] Testing: partyCumulativeLevelCanBeRetrieved");
    RegisterRequest registerRequest = new RegisterRequest("levelUser", "password123");
    authService.register(registerRequest);

    campaignService.startNewCampaign("levelUser", "Hero", HeroClass.WARRIOR);
    int cumulativeLevel = campaignService.getPartyCumulativeLevel("levelUser");

    System.out.println("Party cumulative level retrieved: " + cumulativeLevel);
    System.out.println("  - Level is non-negative: " + (cumulativeLevel >= 0));

    assertThat(cumulativeLevel).isGreaterThanOrEqualTo(0);
  }

  @Test
  void campaignScoreStartsAtZero() {
    System.out.println("\n[DEMO 11] Testing: campaignScoreStartsAtZero");
    RegisterRequest registerRequest = new RegisterRequest("scoreUser", "password123");
    authService.register(registerRequest);

    Campaign campaign =
        campaignService.startNewCampaign("scoreUser", "Hero", HeroClass.WARRIOR);

    System.out.println("Campaign starts with zero score: " + campaign.getScore());

    assertThat(campaign.getScore()).isZero();
  }

  @Test
  void allHeroClassesCanStartCampaign() {
    System.out.println("\n[DEMO 12] Testing: allHeroClassesCanStartCampaign");
    int classCount = 0;
    for (HeroClass heroClass : HeroClass.values()) {
      userRepository.deleteAll();
      campaignRepository.deleteAll();
      RegisterRequest request = new RegisterRequest("userForClass", "password");
      authService.register(request);

      Campaign campaign =
          campaignService.startNewCampaign("userForClass", "Hero", heroClass);

      System.out.println("   " + heroClass.name() + " - Campaign ID: "
          + campaign.getId() + ", Active: " + campaign.isActive());
      classCount++;

      assertThat(campaign).isNotNull();
      assertThat(campaign.isActive()).isTrue();
    }
    System.out.println("Total hero classes tested: " + classCount);
  }
}
