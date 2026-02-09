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

    Campaign campaign = campaignService.startNewCampaign(user.getId(), HeroClass.MAGE, "Arcanis");

    assertNotNull(campaign.getId());
  }

  @Test
  void newCampaignStartsAtRoomOne() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user.getId(), HeroClass.WARRIOR, "Thor");

    assertEquals(1, campaign.getCurrentRoom());
  }

  @Test
  void newCampaignIsActive() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user.getId(), HeroClass.ROGUE, "Luna");

    assertTrue(campaign.isActive());
  }

  @Test
  void startingCampaignCreatesNewHeroForUser() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user.getId(), HeroClass.CLERIC, "Nova");

    assertEquals(user, campaign.getOwner());
    assertEquals("Nova", campaign.getParty().getHeroes().get(0).getName());
  }
}
