package com.university.project.legendsofswordandwand.service.party.impl;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.party.IPartyManagementService;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link IPartyManagementService}, handling party retrieval, healing,
 * gold management, and hero roster changes.
 */
@Service
@Transactional
@RequiredArgsConstructor
class PartyManagementServiceImpl implements IPartyManagementService {

  private final PartyRepository partyRepository;
  private final CampaignRepository campaignRepository;
  private final IHeroService heroService;

  /**
   * Returns the active party for a given campaign.
   *
   * @param campaignId the ID of the campaign
   * @return the active {@link Party}
   * @throws RuntimeException if no party is found for the campaign
   */
  @Override
  public Party getActiveParty(Long campaignId) {
    return campaignRepository
        .findActivePartyByCampaignId(campaignId)
        .orElseThrow(() -> new RuntimeException("Party not found for campaign " + campaignId));
  }

  /**
   * Revives and heals the party for a given campaign, restoring all heroes to full HP and mana.
   *
   * @param campaignId the ID of the campaign whose party to heal
   * @return the saved {@link Party}
   * @throws RuntimeException if no party is found for the campaign
   */
  @Override
  public Party reviveAndHealParty(Long campaignId) {
    Party party =
        campaignRepository
            .findActivePartyByCampaignId(campaignId)
            .orElseThrow(() -> new RuntimeException("Party not found for campaign " + campaignId));

    party
        .getHeroes()
        .forEach(
            hero -> {
              hero.setHealth(hero.getMaxHealth());
              hero.setMana(hero.getMaxMana());
            });

    return partyRepository.save(party);
  }

  /**
   * Deducts the given amount of gold from the specified party.
   *
   * @param partyId the ID of the party
   * @param amount the amount of gold to deduct
   * @throws RuntimeException if the party is not found
   */
  @Override
  public void deductGold(Long partyId, int amount) {

    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party not found"));

    party.setGold(party.getGold() - amount);
    partyRepository.save(party);
  }

  /**
   * Adds the given amount of gold to the specified party.
   *
   * @param partyId the ID of the party
   * @param amount the amount of gold to add
   * @throws RuntimeException if the party is not found
   */
  @Override
  public void addGold(Long partyId, int amount) {

    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party not found"));

    party.setGold(party.getGold() + amount);
    partyRepository.save(party);
  }

  /**
   * Adds the given hero to the specified party's hero roster.
   *
   * @param partyId the ID of the party to add the hero to
   * @param heroId the ID of the hero to add
   * @throws RuntimeException if the party or hero is not found
   */
  @Override
  public void addHeroToParty(Long partyId, Long heroId) {

    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party not found"));
    Hero hero =
        heroService.findById(heroId).orElseThrow(() -> new RuntimeException("Hero not found"));

    party.getHeroes().add(hero);
    partyRepository.save(party);
  }

  /**
   * Revives and heals all permanent heroes in the party and returns a human-readable summary.
   *
   * <p>For each permanent hero: if they were dead, a revival message is added; if they gained HP or
   * mana, the amounts restored are described; if they were already at full health, that is noted.
   * Temporary heroes are excluded from healing and the summary.
   *
   * @param campaignId the ID of the campaign whose party to heal
   * @return a list of summary strings describing what was restored for each hero
   * @throws RuntimeException if no party is found for the campaign
   */
  @Override
  public List<String> reviveAndHealPartyWithSummary(Long campaignId) {

    Party party =
        campaignRepository
            .findActivePartyByCampaignId(campaignId)
            .orElseThrow(() -> new RuntimeException("Party not found"));

    List<String> summary = new ArrayList<>();

    party.getHeroes().stream()
        .filter(h -> !h.isTemporary())
        .forEach(
            hero -> {
              int hpBefore = hero.getHealth();
              int manaBefore = hero.getMana();
              boolean wasDead = hpBefore <= 0;

              hero.setHealth(hero.getMaxHealth());
              hero.setMana(hero.getMaxMana());

              if (wasDead) {
                summary.add(hero.getName() + " was revived and fully restored.");
              } else {
                int hpGained = hero.getMaxHealth() - hpBefore;
                int manaGained = hero.getMaxMana() - manaBefore;
                if (hpGained > 0 || manaGained > 0) {
                  summary.add(
                      hero.getName()
                          + " restored "
                          + (hpGained > 0 ? hpGained + " HP" : "")
                          + (hpGained > 0 && manaGained > 0 ? " and " : "")
                          + (manaGained > 0 ? manaGained + " MP" : "")
                          + ".");
                } else {
                  summary.add(hero.getName() + " was already at full health.");
                }
              }
            });

    partyRepository.save(party);
    return summary;
  }
}
