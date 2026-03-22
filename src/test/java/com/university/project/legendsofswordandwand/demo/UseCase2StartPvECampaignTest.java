package com.university.project.legendsofswordandwand.demo;

import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.auth.IAuthService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=demo")
class UseCase2StartPvECampaignTest {

  @Autowired
  private ICampaignService campaignService;

  @Autowired
  private IAuthService authService;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  void campaignCanBeStartedSuccessfully() {
    RegisterRequest registerRequest = new RegisterRequest("campaignUser", "password123");
    authService.register(registerRequest);

    Campaign campaign =
        campaignService.startNewCampaign("campaignUser", "Aragorn", HeroClass.WARRIOR);

    assertThat(campaign).isNotNull();
    assertThat(campaign.getId()).isNotNull();
    assertThat(campaign.isActive()).isTrue();
  }

  @Test
  void campaignStartsAtRoom0() {
    RegisterRequest registerRequest = new RegisterRequest("roomUser", "password123");
    authService.register(registerRequest);

    Campaign campaign =
        campaignService.startNewCampaign("roomUser", "Legolas", HeroClass.RANGER);

    assertThat(campaign.getCurrentRoom()).isZero();
  }

  @Test
  void campaignHasPartyWithStartingHero() {
    RegisterRequest registerRequest = new RegisterRequest("partyUser", "password123");
    authService.register(registerRequest);

    Campaign campaign =
        campaignService.startNewCampaign("partyUser", "Gimli", HeroClass.WARRIOR);

    assertThat(campaign.getParty()).isNotNull();
    assertThat(campaign.getParty().getHeroes()).isNotEmpty();
  }

  @Test
  void campaignHasOwner() {
    RegisterRequest registerRequest = new RegisterRequest("ownerUser", "password123");
    authService.register(registerRequest);

    Campaign campaign =
        campaignService.startNewCampaign("ownerUser", "Gandalf", HeroClass.MAGE);

    assertThat(campaign.getOwner()).isNotNull();
    assertThat(campaign.getOwner().getUsername()).isEqualTo("ownerUser");
  }

  @Test
  void cannotStartCampaignWithoutRegistration() {
    assertThatThrownBy(
            () ->
                campaignService.startNewCampaign("unregisteredUser", "Hero", HeroClass.WARRIOR))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  void cannotStartMultipleCampaignsSametime() {
    RegisterRequest registerRequest = new RegisterRequest("multiCampaignUser", "password123");
    authService.register(registerRequest);

    campaignService.startNewCampaign("multiCampaignUser", "FirstHero", HeroClass.WARRIOR);

    assertThatThrownBy(
            () ->
                campaignService.startNewCampaign(
                    "multiCampaignUser", "SecondHero", HeroClass.MAGE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Campaign already in progress");
  }

  @Test
  void activeCampaignCanBeRetrieved() {
    RegisterRequest registerRequest = new RegisterRequest("retrieveUser", "password123");
    authService.register(registerRequest);

    Campaign startedCampaign =
        campaignService.startNewCampaign("retrieveUser", "Hero", HeroClass.WARRIOR);
    Campaign retrievedCampaign = campaignService.getActiveCampaign("retrieveUser");

    assertThat(retrievedCampaign.getId()).isEqualTo(startedCampaign.getId());
  }

  @Test
  void userHasActiveCampaignAfterStart() {
    RegisterRequest registerRequest = new RegisterRequest("activeCampaignUser", "password123");
    var user = authService.register(registerRequest);

    assertThat(campaignService.hasActiveCampaign(user.getId())).isFalse();

    campaignService.startNewCampaign("activeCampaignUser", "Hero", HeroClass.WARRIOR);

    assertThat(campaignService.hasActiveCampaign(user.getId())).isTrue();
  }

  @Test
  void differentUsersCanStartCampaignsSeparately() {
    RegisterRequest user1 = new RegisterRequest("user1", "pass1");
    RegisterRequest user2 = new RegisterRequest("user2", "pass2");
    authService.register(user1);
    authService.register(user2);

    Campaign campaign1 =
        campaignService.startNewCampaign("user1", "Hero1", HeroClass.WARRIOR);
    Campaign campaign2 =
        campaignService.startNewCampaign("user2", "Hero2", HeroClass.MAGE);

    assertThat(campaign1.getId()).isNotEqualTo(campaign2.getId());
    assertThat(campaign1.getOwner().getUsername()).isNotEqualTo(campaign2.getOwner().getUsername());
  }

  @Test
  void partyCumulativeLevelCanBeRetrieved() {
    RegisterRequest registerRequest = new RegisterRequest("levelUser", "password123");
    authService.register(registerRequest);

    campaignService.startNewCampaign("levelUser", "Hero", HeroClass.WARRIOR);
    int cumulativeLevel = campaignService.getPartyCumulativeLevel("levelUser");

    assertThat(cumulativeLevel).isGreaterThanOrEqualTo(0);
  }

  @Test
  void campaignScoreStartsAtZero() {
    RegisterRequest registerRequest = new RegisterRequest("scoreUser", "password123");
    authService.register(registerRequest);

    Campaign campaign =
        campaignService.startNewCampaign("scoreUser", "Hero", HeroClass.WARRIOR);

    assertThat(campaign.getScore()).isZero();
  }

  @Test
  void allHeroClassesCanStartCampaign() {
    for (HeroClass heroClass : HeroClass.values()) {
      userRepository.deleteAll();
      RegisterRequest request = new RegisterRequest("userForClass", "password");
      authService.register(request);

      Campaign campaign =
          campaignService.startNewCampaign("userForClass", "Hero", heroClass);

      assertThat(campaign).isNotNull();
      assertThat(campaign.isActive()).isTrue();
    }
  }
}
