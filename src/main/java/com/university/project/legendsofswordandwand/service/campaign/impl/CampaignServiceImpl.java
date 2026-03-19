package com.university.project.legendsofswordandwand.service.campaign.impl;

import com.university.project.legendsofswordandwand.model.*;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.HeroRepository;
import com.university.project.legendsofswordandwand.repository.ItemRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.party.IPartyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Campaign Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
class CampaignServiceImpl implements ICampaignService {

  private final CampaignRepository campaignRepository;
  private final UserRepository userRepository;
  private final IHeroService heroService;
  private final IPartyService partyService;
  private final ItemRepository itemRepository;
  private final HeroRepository heroRepository;

  @Override
  public boolean hasActiveCampaign(Long userId) {
    return campaignRepository.existsActiveCampaignByOwnerId(userId);
  }

  @Override
  public Campaign getActiveCampaign(String username) {
    return campaignRepository
        .findActiveCampaignByUsername(username)
        .orElseThrow(() -> new RuntimeException("No active campaign found"));
  }

  @Override
  public Campaign exitCampaign(String username) {

    Campaign campaign = getActiveCampaign(username);
    return campaignRepository.save(campaign);
  }

  @Override
  public Campaign startNewCampaign(String username, String heroName, HeroClass heroClass) {

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    if (hasActiveCampaign(user.getId())) throw new RuntimeException("Campaign already in progress");

    Party party = partyService.createPartyForUser(user.getId());
    heroService.createBaseHeroForParty(party.getId(), heroName, heroClass);

    Campaign campaign =
        Campaign.builder().owner(user).active(true).currentRoom(0).party(party).build();

    return campaignRepository.save(campaign);
  }

  @Override
  public void savePartyFromCampaign(Long campaignId, Long userId) {

    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    long savedCount = user.getParties().stream().filter(Party::isSaved).count();

    if (savedCount >= 5)
      throw new RuntimeException("Already have 5 saved parties - replace one first");

    Campaign campaign =
        campaignRepository
            .findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));

    campaign.getParty().setSaved(true);
    campaign.setActive(false);
    campaignRepository.save(campaign);
  }

  @Override
  public void replacePartyFromCampaign(Long campaignId, Long userId, Long partyIdToReplace) {

    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    Party toReplace =
        user.getParties().stream()
            .filter(p -> p.getId().equals(partyIdToReplace) && p.isSaved())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Saved party not found"));

    user.getParties().remove(toReplace);
    partyService.deleteParty(toReplace.getId());
    userRepository.save(user);

    savePartyFromCampaign(campaignId, userId);
  }

  @Override
  public int getPartyCumulativeLevel(String username) {

    Campaign campaign = getActiveCampaign(username);
    return campaign.getParty().getCumulativeLevel();
  }

  @Override
  public void abandonCampaign(String username) {
    Campaign campaign = getActiveCampaign(username);
    campaign.getParty().getHeroes().stream()
            .filter(Hero::isTemporary)
            .toList()
            .forEach(h -> heroRepository.deleteById(h.getId()));
    campaign.setActive(false);
    campaignRepository.save(campaign);
  }

  @Override
  public Campaign completeCampaign(String username) {

    Campaign campaign = getActiveCampaign(username);

    int baseScore = campaign.getParty().calculateScore();

    int itemScore = 0;
    Inventory inventory = campaign.getParty().getInventory();
    if (inventory != null) {

      itemScore = inventory.getItemIds().stream()
              .mapToInt(id -> itemRepository.findById(id)
                      .map(item -> (item.getCost() / 2) * 10)
                      .orElse(0))
              .sum();
    }

    campaign.getParty().getHeroes().stream()
            .filter(Hero::isTemporary)
            .toList()
            .forEach(h -> heroRepository.deleteById(h.getId()));

    campaign.setScore(baseScore + itemScore);
    campaign.setActive(false);
    return campaignRepository.save(campaign);
  }

  @Override
  public Campaign getMostRecentCompletedCampaign(String username) {

    return campaignRepository.findCompletedByOwnerUsername(username).stream()
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No completed campaign found"));
  }
}
