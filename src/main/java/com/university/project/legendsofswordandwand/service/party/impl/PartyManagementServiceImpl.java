package com.university.project.legendsofswordandwand.service.party.impl;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.service.hero.IHeroService;
import com.university.project.legendsofswordandwand.service.party.IPartyManagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
class PartyManagementServiceImpl implements IPartyManagementService {

  private final PartyRepository partyRepository;
  private final CampaignRepository campaignRepository;
  private final IHeroService heroService;

  /** Returns the active party for a given campaign. */
  @Override
  public Party getActiveParty(Long campaignId) {
    return campaignRepository
        .findActivePartyByCampaignId(campaignId)
        .orElseThrow(() -> new RuntimeException("Party not found for campaign " + campaignId));
  }

  /** Revives and heals the party for a given campaign. */
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

  @Override
  public void deductGold(Long partyId, int amount) {

    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party not found"));

    party.setGold(party.getGold() - amount);
    partyRepository.save(party);
  }

  @Override
  public void addGold(Long partyId, int amount) {

    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party not found"));

    party.setGold(party.getGold() + amount);
    partyRepository.save(party);
  }

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

  @Override
  public List<String> reviveAndHealPartyWithSummary(Long campaignId) {

    Party party = campaignRepository.findActivePartyByCampaignId(campaignId)
            .orElseThrow(() -> new RuntimeException("Party not found"));

    List<String> summary = new ArrayList<>();

    party.getHeroes().stream().filter(h -> !h.isTemporary()).forEach(hero -> {
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
          summary.add(hero.getName() + " restored "
                  + (hpGained > 0 ? hpGained + " HP" : "")
                  + (hpGained > 0 && manaGained > 0 ? " and " : "")
                  + (manaGained > 0 ? manaGained + " MP" : "") + ".");
        } else {
          summary.add(hero.getName() + " was already at full health.");
        }
      }
    });

    partyRepository.save(party);
    return summary;
  }
}
