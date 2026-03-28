package com.university.project.legendsofswordandwand.service.campaign;

import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;

/**
 * Service interface defining the contract for campaign lifecycle management, including creation,
 * persistence, completion, and party saving.
 */
public interface ICampaignService {

  /**
   * Starts a new campaign for the given user, creating a new party and starting hero.
   *
   * @param username the username of the player
   * @param heroName the name of the starting hero
   * @param heroClass the {@link HeroClass} of the starting hero
   * @return the newly created and persisted {@link Campaign}
   * @throws RuntimeException if the user already has an active campaign or is not found
   */
  Campaign startNewCampaign(String username, String heroName, HeroClass heroClass);

  /**
   * Returns {@code true} if the given user currently has an active campaign.
   *
   * @param userId the ID of the user to check
   * @return {@code true} if an active campaign exists
   */
  boolean hasActiveCampaign(Long userId);

  /**
   * Retrieves the active campaign for the given username.
   *
   * @param username the username of the player
   * @return the active {@link Campaign}
   * @throws RuntimeException if no active campaign is found
   */
  Campaign getActiveCampaign(String username);

  /**
   * Saves the current campaign state to the database without deactivating it, allowing the player
   * to resume later.
   *
   * @param username the username of the player
   * @return the saved {@link Campaign}
   */
  Campaign exitCampaign(String username);

  /**
   * Marks the active campaign as complete, calculates the final score, and deactivates it.
   *
   * @param username the username of the player
   * @return the completed and deactivated {@link Campaign}
   */
  Campaign completeCampaign(String username);

  /**
   * Retrieves the most recently completed campaign for the given player.
   *
   * @param username the username of the player
   * @return the most recently completed {@link Campaign}, or {@code null} if none exists
   */
  Campaign getMostRecentCompletedCampaign(String username);

  /**
   * Saves the party from the given campaign as a permanent saved party on the player's profile. All
   * heroes are restored to full HP and mana before saving.
   *
   * @param campaignId the ID of the campaign whose party to save
   * @param userId the ID of the user to associate the saved party with
   * @throws RuntimeException if the player already has 5 saved parties
   */
  void savePartyFromCampaign(Long campaignId, Long userId);

  /**
   * Saves the campaign party, replacing an existing saved party on the player's profile.
   *
   * @param campaignId the ID of the campaign whose party to save
   * @param userId the ID of the user
   * @param partyIdToReplace the ID of the saved party to permanently remove and replace
   */
  void replacePartyFromCampaign(Long campaignId, Long userId, Long partyIdToReplace);

  /**
   * Returns the sum of all hero levels in the active campaign party.
   *
   * @param username the username of the player
   * @return the cumulative level total
   */
  int getPartyCumulativeLevel(String username);

  /**
   * Permanently abandons the active campaign, deleting the associated party and heroes.
   *
   * @param username the username of the player
   */
  void abandonCampaign(String username);
}
