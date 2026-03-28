package com.university.project.legendsofswordandwand.service.party;

import com.university.project.legendsofswordandwand.model.Party;
import java.util.List;

/**
 * Service interface defining the contract for managing the active campaign party, including
 * healing, gold, and hero roster operations.
 */
public interface IPartyManagementService {

  /**
   * Retrieves the active party associated with the given campaign.
   *
   * @param campaignId the ID of the campaign
   * @return the active {@link Party}
   * @throws RuntimeException if the campaign or its party is not found
   */
  Party getActiveParty(Long campaignId);

  /**
   * Revives all dead heroes and restores full HP and mana to every hero in the party.
   *
   * @param campaignId the ID of the campaign whose party to heal
   * @return the updated {@link Party}
   */
  Party reviveAndHealParty(Long campaignId);

  /**
   * Deducts the given amount of gold from the party treasury, floored at zero.
   *
   * @param partyId the ID of the party
   * @param amount the amount of gold to deduct
   */
  void deductGold(Long partyId, int amount);

  /**
   * Adds the given amount of gold to the party treasury.
   *
   * @param partyId the ID of the party
   * @param amount the amount of gold to add
   */
  void addGold(Long partyId, int amount);

  /**
   * Adds the given hero to the party's hero roster.
   *
   * @param partyId the ID of the party
   * @param heroId the ID of the hero to add
   */
  void addHeroToParty(Long partyId, Long heroId);

  /**
   * Revives and heals all heroes in the campaign party and returns a human-readable summary of the
   * HP and mana restored to each hero.
   *
   * @param campaignId the ID of the campaign whose party to heal
   * @return a list of log strings describing the healing applied to each hero
   */
  List<String> reviveAndHealPartyWithSummary(Long campaignId);
}
