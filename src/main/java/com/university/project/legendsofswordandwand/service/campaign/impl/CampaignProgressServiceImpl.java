package com.university.project.legendsofswordandwand.service.campaign.impl;

import com.university.project.legendsofswordandwand.dto.response.CampaignViewInfo;
import com.university.project.legendsofswordandwand.dto.response.CompleteCampaignInfo;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.model.Campaign;
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

@Service
@Transactional
@RequiredArgsConstructor
class CampaignProgressServiceImpl implements ICampaignProgressService {

  private final ICampaignService campaignService;
  private final CampaignRepository campaignRepository;
  private final Random random = new Random();

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
    campaignRepository.save(campaign);

    return roomType;
  }

  public boolean isCampaignComplete(String username) {

    return campaignService.getActiveCampaign(username).getCurrentRoom() >= 30;
  }

  @Override
  public int calculateAndPersistScore(Campaign campaign) {

    int score = campaign.getParty().calculateScore();

    campaign.setScore(score);
    campaignRepository.save(campaign);
    return score;
  }

  @Override
  public CampaignViewInfo getCampaignViewData(String username) {

    Campaign campaign = campaignService.getActiveCampaign(username);

    return new CampaignViewInfo(
        campaign.getId(),
        campaign.getCurrentRoom(),
        campaign.getParty().getGold(),
        campaign.getParty().getHeroes());
  }

  @Override
  public CompleteCampaignInfo getCompletionData(String username) {

    Campaign campaign = campaignService.getMostRecentCompletedCampaign(username);

    List<Party> savedParties =
        campaign.getOwner().getParties().stream().filter(Party::isSaved).toList();

    return new CompleteCampaignInfo(
        campaign.getId(),
        campaign.getScore(),
        campaign.getParty().getGold(),
        campaign.getParty().getHeroes(),
        savedParties.size() >= 5,
        savedParties);
  }

  @Override
  public List<ProfileInfo.CampaignResultInfo> getCampaignResultsForUser(Long userId) {
    return campaignRepository.findAllByOwnerIdOrderByScoreDesc(userId).stream()
        .map(
            c -> new ProfileInfo.CampaignResultInfo(c.getScore(), c.getCurrentRoom(), c.isActive()))
        .toList();
  }
}
