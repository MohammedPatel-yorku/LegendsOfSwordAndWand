package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Campaign Object Service class. */
@Service
@RequiredArgsConstructor
public class CampaignService {

  private final CampaignRepository campaignRepository;
  private final HeroService heroService;
  private final PartyService partyService;
  private final UserRepository userRepository;

  /**
   * Creates new Campaign Object, sends it to CampaignRepository to save and creates a new Party and
   * Hero for the requesting user.
   *
   * @param userId ID of User that is starting a new Campaign
   * @param selectedHeroClass Hero Class to assign to starting Hero
   * @param selectedHeroName Name to assign to starting Hero
   * @return Newly created Campaign Object
   */
  public Campaign startNewCampaign(
      Long userId, HeroClass selectedHeroClass, String selectedHeroName) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    Party party = partyService.createPartyForUser(userId);

    heroService.createBaseHeroForParty(party.getId(), selectedHeroName, selectedHeroClass);

    Campaign campaign =
        Campaign.builder().owner(user).active(true).currentRoom(1).party(party).build();

    return campaignRepository.save(campaign);
  }
}
