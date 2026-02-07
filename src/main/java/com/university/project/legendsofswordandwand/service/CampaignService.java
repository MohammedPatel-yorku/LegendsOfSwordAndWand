package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Campaign Object Service class. */
@Service
@RequiredArgsConstructor
public class CampaignService {

  private final CampaignRepository campaignRepository;
  private final HeroService heroService;
  private final PartyService partyService;

  /**
   * Creates new Campaign Object, sends it to CampaignRepository to save and creates a new Hero for
   * the requesting user.
   *
   * @param owner User that is starting a new Campaign
   * @param selectedHeroClass Hero Class to assign to starting Hero
   * @param selectedHeroName Name to assign to starting Hero
   * @return Newly created Campaign Object
   */
  public Campaign startNewCampaign(
      User owner, HeroClass selectedHeroClass, String selectedHeroName) {

    Hero hero =
        heroService.createBaseHeroForUser(owner.getId(), selectedHeroName, selectedHeroClass);

    Party party = partyService.createNewParty(owner, hero);

    Campaign campaign = new Campaign();
    campaign.setOwner(owner);
    campaign.setActive(true);
    campaign.setCurrentRoom(1);
    campaign.setParty(party);

    campaignRepository.save(campaign);
    return campaign;
  }
}
