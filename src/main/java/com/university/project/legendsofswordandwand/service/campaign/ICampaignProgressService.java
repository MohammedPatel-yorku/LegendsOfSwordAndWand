package com.university.project.legendsofswordandwand.service.campaign;

import com.university.project.legendsofswordandwand.dto.response.CampaignViewInfo;
import com.university.project.legendsofswordandwand.dto.response.CompleteCampaignInfo;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.enums.RoomType;
import java.util.List;

/** Service interface defining the contract for advancing and querying campaign room progress. */
public interface ICampaignProgressService {

  /**
   * Advances the campaign to the next room and determines whether it is a battle or an inn. The
   * room type is persisted on the campaign so the player can resume if they disconnect.
   *
   * @param username the username of the player
   * @return the {@link RoomType} of the room entered
   */
  RoomType enterNextRoom(String username);

  /**
   * Returns {@code true} if the player's active campaign has reached or exceeded 30 rooms.
   *
   * @param username the username of the player
   * @return {@code true} if the campaign is complete
   */
  boolean isCampaignComplete(String username);

  /**
   * Calculates and persists the final score for a completed campaign. Score formula: {@code 100 ×
   * hero levels + 10 × gold + item bonus}.
   *
   * @param campaign the completed {@link Campaign} to score
   * @return the computed final score
   */
  int calculateAndPersistScore(Campaign campaign);

  /**
   * Retrieves a summary of all completed campaign results for the given user, used to populate the
   * profile view.
   *
   * @param userId the ID of the user
   * @return a list of {@link ProfileInfo.CampaignResultInfo} records
   */
  List<ProfileInfo.CampaignResultInfo> getCampaignResultsForUser(Long userId);

  /**
   * Assembles the full campaign completion view data, including score, heroes, gold, and
   * saved-party state.
   *
   * @param username the username of the player
   * @return a {@link CompleteCampaignInfo} DTO
   */
  CompleteCampaignInfo getCompletionData(String username);

  /**
   * Assembles the current campaign view data including the active room, heroes, and gold.
   *
   * @param username the username of the player
   * @return a {@link CampaignViewInfo} DTO
   */
  CampaignViewInfo getCampaignViewData(String username);

  /**
   * Clears the pending room flag on the active campaign, indicating the player has fully entered
   * and acknowledged the current room.
   *
   * @param username the username of the player
   */
  void clearRoomPending(String username);
}
