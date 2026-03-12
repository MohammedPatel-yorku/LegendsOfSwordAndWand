package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
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

  /** Recruit a hero into the party for a given campaign. */
  @Override
  public Party recruitHero(Long campaignId, Long heroId) {
    Party party =
        campaignRepository
            .findActivePartyByCampaignId(campaignId)
            .orElseThrow(() -> new RuntimeException("Party not found for campaign " + campaignId));

    if (party.getHeroes().size() >= 5) throw new RuntimeException("Party is full");

    return partyRepository.save(party);
  }
}
