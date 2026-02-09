package com.university.project.legendsofswordandwand.service;

import static org.junit.jupiter.api.Assertions.*;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CampaignServiceTest {

  @Autowired private CampaignService campaignService;

  @Autowired private UserService userService;

  @Test
  void startingCampaignCreatesCampaign() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user.getId(), "Harry", HeroClass.MAGE);

    assertNotNull(campaign.getId());
  }

  @Test
  void newCampaignStartsAtRoomOne() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user.getId(), "Thor", HeroClass.WARRIOR);

    assertEquals(1, campaign.getCurrentRoom());
  }

  @Test
  void newCampaignIsActive() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user.getId(), "Luna", HeroClass.ROGUE);

    assertTrue(campaign.isActive());
  }

  @Test
  void startingCampaignOwnedByUserAndPartyAndHeroAdded() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user.getId(), "Nova", HeroClass.CLERIC);

    assertEquals(user, campaign.getOwner());
    assertEquals("Nova", campaign.getParty().getHeroes().get(0).getName());
  }
}
