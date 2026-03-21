package com.university.project.legendsofswordandwand.service.campaign.impl;

import com.university.project.legendsofswordandwand.dto.response.CampaignViewInfo;
import com.university.project.legendsofswordandwand.dto.response.CompleteCampaignInfo;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.enums.RoomType;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignProgressService;
import com.university.project.legendsofswordandwand.service.campaign.ICampaignService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link ICampaignProgressService}, managing room progression, campaign
 * completion checks, score calculation, and view data assembly.
 */
@Service
@Transactional
@RequiredArgsConstructor
class CampaignProgressServiceImpl implements ICampaignProgressService {

  private final ICampaignService campaignService;
  private final CampaignRepository campaignRepository;
  private final Random random = new Random();

  /**
   * Advances the campaign to the next room and returns its type.
   *
   * <p>If a room is already pending (i.e. the player has entered but not completed it), the same
   * room type is returned without advancing. Otherwise, a new room type is randomly determined: the
   * chance of a battle room scales with the party's cumulative level, starting at 60% and capping
   * at 90%. Inn rooms mark {@code hasVisitedInn} as true.
   *
   * @param username the username of the player
   * @return the {@link RoomType} of the next room
   * @throws RuntimeException if the campaign has already reached room 30
   */
  @Override
  public RoomType enterNextRoom(String username) {

    Campaign campaign = campaignService.getActiveCampaign(username);

    if (campaign.getCurrentRoom() >= 30) throw new RuntimeException("Campaign is already complete");

    if (campaign.isRoomPending()) return campaign.getLastRoomType();

    int cumulativeLevel = campaign.getParty().getCumulativeLevel();
    int battleChance = Math.min(90, 60 + (cumulativeLevel / 10) * 3);
    RoomType roomType = random.nextInt(100) < battleChance ? RoomType.BATTLE : RoomType.INN;

    campaign.setCurrentRoom(campaign.getCurrentRoom() + 1);
    campaign.setLastRoomType(roomType);
    campaign.setRoomPending(true);
    if (roomType == RoomType.INN) campaign.setHasVisitedInn(true);
    campaignRepository.save(campaign);

    return roomType;
  }

  /**
   * Returns {@code true} if the player's active campaign has reached or passed room 30.
   *
   * @param username the username of the player
   * @return {@code true} if the campaign is complete
   */
  public boolean isCampaignComplete(String username) {
    return campaignService.getActiveCampaign(username).getCurrentRoom() >= 30;
  }

  /**
   * Calculates the campaign score from the party's current state, persists it, and returns it.
   *
   * @param campaign the {@link Campaign} to score
   * @return the calculated score
   */
  @Override
  public int calculateAndPersistScore(Campaign campaign) {

    int score = campaign.getParty().calculateScore();

    campaign.setScore(score);
    campaignRepository.save(campaign);
    return score;
  }

  /**
   * Assembles and returns the view data for the campaign page.
   *
   * <p>Includes the campaign ID, current room, gold, and all non-temporary heroes.
   *
   * @param username the username of the player
   * @return a {@link CampaignViewInfo} populated with the current campaign state
   */
  @Override
  public CampaignViewInfo getCampaignViewData(String username) {
    Campaign campaign = campaignService.getActiveCampaign(username);
    List<Hero> permanentHeroes =
        campaign.getParty().getHeroes().stream().filter(h -> !h.isTemporary()).toList();
    return new CampaignViewInfo(
        campaign.getId(),
        campaign.getCurrentRoom(),
        campaign.getParty().getGold(),
        permanentHeroes);
  }

  /**
   * Clears the pending room flag on the player's active campaign.
   *
   * <p>Should be called after a room has been successfully completed.
   *
   * @param username the username of the player
   */
  @Override
  public void clearRoomPending(String username) {

    Campaign campaign = campaignService.getActiveCampaign(username);
    campaign.setRoomPending(false);
    campaignRepository.save(campaign);
  }

  /**
   * Assembles and returns completion data for the campaign completion page.
   *
   * <p>Retrieves the most recently completed campaign for the user and includes its score, gold,
   * permanent heroes, and the player's existing saved parties.
   *
   * @param username the username of the player
   * @return a {@link CompleteCampaignInfo} populated with completion data
   */
  @Override
  public CompleteCampaignInfo getCompletionData(String username) {

    Campaign campaign = campaignService.getMostRecentCompletedCampaign(username);

    List<Party> savedParties =
        campaign.getOwner().getParties().stream().filter(Party::isSaved).toList();

    return new CompleteCampaignInfo(
        campaign.getId(),
        campaign.getScore(),
        campaign.getParty().getGold(),
        campaign.getParty().getHeroes().stream().filter(h -> !h.isTemporary()).toList(),
        savedParties.size() >= 5,
        savedParties);
  }

  /**
   * Returns a list of campaign result summaries for the given user, ordered by score descending.
   *
   * @param userId the ID of the user
   * @return a list of {@link ProfileInfo.CampaignResultInfo} records
   */
  @Override
  public List<ProfileInfo.CampaignResultInfo> getCampaignResultsForUser(Long userId) {
    return campaignRepository.findAllByOwnerIdOrderByScoreDesc(userId).stream()
        .map(
            c -> new ProfileInfo.CampaignResultInfo(c.getScore(), c.getCurrentRoom(), c.isActive()))
        .toList();
  }
}
