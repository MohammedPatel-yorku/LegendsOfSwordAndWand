package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.IHeroService;
import com.university.project.legendsofswordandwand.service.IPartyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Party Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
public class PartyServiceImpl implements IPartyService {

  private final PartyRepository partyRepository;
  private final UserRepository userRepository;
  private final CampaignRepository campaignRepository;
  private final IHeroService heroService;

  /** Creates a new Party for User to use in a newly created Campaign. */
  @Override
  public Party createPartyForUser(Long userId) {
    User owner =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

    Party party = Party.builder().owner(owner).build();

    owner.getParties().add(party);
    return partyRepository.save(party);
  }

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
  public void deleteParty(Long partyId) {

    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party not found"));

    partyRepository.delete(party);
  }

  @Override
  public void updateGold(Long partyId, int cost) {

    Party party =
        partyRepository
            .findById(partyId)
            .orElseThrow(() -> new RuntimeException("Party not found"));

    party.setGold(party.getGold() - cost);
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
}
