package com.university.project.legendsofswordandwand.service;

import static org.junit.jupiter.api.Assertions.*;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
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

  @Autowired private CampaignRepository campaignRepository;

  @Test
  void startingCampaignCreatesCampaign() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user, HeroClass.MAGE, "Arcanis");

    assertNotNull(campaign.getId());
  }

  @Test
  void newCampaignStartsAtRoomOne() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user, HeroClass.WARRIOR, "Thor");

    assertEquals(1, campaign.getCurrentRoom());
  }

  @Test
  void newCampaignIsActive() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user, HeroClass.ROGUE, "Luna");

    assertTrue(campaign.isActive());
  }

  @Test
  void startingCampaignCreatesNewHeroForUser() {
    User user = userService.createUser("user", "pass");

    Campaign campaign = campaignService.startNewCampaign(user, HeroClass.CLERIC, "Nova");

    assertEquals(1, user.getHeroes().size());
    assertEquals("Nova", user.getHeroes().get(0).getName());
  }
}
