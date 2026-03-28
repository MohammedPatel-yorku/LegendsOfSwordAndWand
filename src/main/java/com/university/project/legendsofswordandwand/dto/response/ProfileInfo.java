package com.university.project.legendsofswordandwand.dto.response;

import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.util.List;

/**
 * Immutable view DTO carrying the full profile data for the profile page.
 *
 * @param username the player's username
 * @param pvpWins total PvP wins recorded for this player
 * @param pvpLosses total PvP losses recorded for this player
 * @param parties the player's saved parties and their heroes
 * @param campaignResults a history of the player's completed (and active) campaign runs
 */
public record ProfileInfo(
    String username,
    int pvpWins,
    int pvpLosses,
    List<PartyInfo> parties,
    List<CampaignResultInfo> campaignResults) {

  /**
   * Immutable summary of a single saved party.
   *
   * @param id the party's database ID
   * @param gold the gold currently held by the party
   * @param cumulativeLevel the sum of all hero levels in the party
   * @param heroes the heroes belonging to this party
   */
  public record PartyInfo(Long id, int gold, int cumulativeLevel, List<HeroInfo> heroes) {}

  /**
   * Immutable summary of a single hero for display on the profile page.
   *
   * @param name the hero's display name
   * @param heroClass the hero's current base class
   * @param hybridClass the hybrid class, if the hero has hybridized; {@code null} otherwise
   * @param hybrid {@code true} if the hero has reached hybrid status
   * @param level the hero's current level
   * @param health the hero's current HP
   * @param attack the hero's attack stat
   * @param defense the hero's defense stat
   * @param mana the hero's current mana
   */
  public record HeroInfo(
      String name,
      HeroClass heroClass,
      HybridClass hybridClass,
      boolean hybrid,
      int level,
      int health,
      int attack,
      int defense,
      int mana) {}

  /**
   * Immutable summary of a single campaign run result.
   *
   * @param score the final score achieved, or 0 if the campaign is still active
   * @param roomsReached the highest room number reached in this run
   * @param active {@code true} if this campaign is still in progress
   */
  public record CampaignResultInfo(int score, int roomsReached, boolean active) {}
}
