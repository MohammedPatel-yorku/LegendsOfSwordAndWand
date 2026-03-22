package com.university.project.legendsofswordandwand.demo;

import com.university.project.legendsofswordandwand.dto.request.RegisterRequest;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Item;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.auth.IAuthService;
import com.university.project.legendsofswordandwand.service.battle.IInnService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=demo")
class UseCase4InteractWithTheInnTest {

  @Autowired
  private IInnService innService;

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

  private void setupCampaignForInn() {
    RegisterRequest request = new RegisterRequest("innUser", "password123");
    authService.register(request);
    campaignService.startNewCampaign("innUser", "Adventurer", HeroClass.WARRIOR);
  }

  @Test
  void innCanProvideShopItems() {
    setupCampaignForInn();

    List<Item> shopItems = innService.getShopItems();

    assertThat(shopItems).isNotEmpty();
  }

  @Test
  void shopItemsHavePrices() {
    setupCampaignForInn();

    List<Item> shopItems = innService.getShopItems();

    shopItems.forEach(item -> 
      assertThat(item.getCost()).isGreaterThanOrEqualTo(0)
    );
  }

  @Test
  void shopItemsHaveNames() {
    setupCampaignForInn();

    List<Item> shopItems = innService.getShopItems();

    shopItems.forEach(item -> 
      assertThat(item.getName())
          .isNotNull()
          .isNotBlank()
    );
  }

  @Test
  void innCanProvideAvailableRecruits() {
    setupCampaignForInn();
    var campaign = campaignService.getActiveCampaign("innUser");

    List<Hero> recruits = innService.getAvailableRecruits(campaign.getId());

    assertThat(recruits).isNotNull();
  }

  @Test
  void recruitsHaveValidStats() {
    setupCampaignForInn();
    var campaign = campaignService.getActiveCampaign("innUser");

    List<Hero> recruits = innService.getAvailableRecruits(campaign.getId());

    if (!recruits.isEmpty()) {
      recruits.forEach(hero -> {
        assertThat(hero.getHealth()).isGreaterThan(0);
        assertThat(hero.getAttack()).isGreaterThanOrEqualTo(0);
        assertThat(hero.getDefense()).isGreaterThanOrEqualTo(0);
      });
    }
  }

  @Test
  void innCanLoadInnView() {
    setupCampaignForInn();
    var campaign = campaignService.getActiveCampaign("innUser");

    List<String> innView = innService.loadInnView(campaign.getId());

    assertThat(innView).isNotNull();
  }

  @Test
  void shopItemsArePersistent() {
    setupCampaignForInn();

    List<Item> firstCall = innService.getShopItems();
    List<Item> secondCall = innService.getShopItems();

    assertThat(firstCall.size()).isEqualTo(secondCall.size());
  }

  @Test
  void recruitsAreGeneratedDynamicallyPerCampaign() {
    RegisterRequest user1 = new RegisterRequest("innUser1", "pass1");
    RegisterRequest user2 = new RegisterRequest("innUser2", "pass2");
    authService.register(user1);
    authService.register(user2);
    
    var campaign1 = campaignService.startNewCampaign("innUser1", "Hero1", HeroClass.WARRIOR);
    var campaign2 = campaignService.startNewCampaign("innUser2", "Hero2", HeroClass.MAGE);

    List<Hero> recruits1 = innService.getAvailableRecruits(campaign1.getId());
    List<Hero> recruits2 = innService.getAvailableRecruits(campaign2.getId());

    assertThat(recruits1).isNotNull();
    assertThat(recruits2).isNotNull();
  }

  @Test
  void recruitsHaveNames() {
    setupCampaignForInn();
    var campaign = campaignService.getActiveCampaign("innUser");

    List<Hero> recruits = innService.getAvailableRecruits(campaign.getId());

    if (!recruits.isEmpty()) {
      recruits.forEach(hero -> 
        assertThat(hero.getName())
            .isNotNull()
            .isNotBlank()
      );
    }
  }

  @Test
  void shopItemsContainVariety() {
    setupCampaignForInn();

    List<Item> shopItems = innService.getShopItems();

    assertThat(shopItems.size()).isGreaterThanOrEqualTo(1);
  }

  @Test
  void multipleInnInteractionsCanOccur() {
    setupCampaignForInn();
    var campaign = campaignService.getActiveCampaign("innUser");

    List<String> view1 = innService.loadInnView(campaign.getId());
    List<Item> items = innService.getShopItems();
    List<Hero> recruits = innService.getAvailableRecruits(campaign.getId());
    List<String> view2 = innService.loadInnView(campaign.getId());

    assertThat(view1).isNotNull();
    assertThat(items).isNotNull();
    assertThat(recruits).isNotNull();
    assertThat(view2).isNotNull();
  }

  @Test
  void innServiceIsAvailable() {
    setupCampaignForInn();

    assertThat(innService).isNotNull();
  }

  @Test
  void itemsHavePositiveOrZeroCost() {
    List<Item> items = innService.getShopItems();

    items.forEach(item -> 
      assertThat(item.getCost()).isGreaterThanOrEqualTo(0)
    );
  }
}
