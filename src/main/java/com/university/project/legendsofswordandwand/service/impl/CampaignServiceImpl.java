package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.dto.response.CampaignViewInfo;
import com.university.project.legendsofswordandwand.dto.response.CompleteCampaignInfo;
import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.RoomType;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.ICampaignService;
import com.university.project.legendsofswordandwand.service.IHeroService;
import com.university.project.legendsofswordandwand.service.IPartyService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Campaign Object Service class. */
@Service
@Transactional
@RequiredArgsConstructor
public class CampaignServiceImpl implements ICampaignService {

  private final CampaignRepository campaignRepository;
  private final UserRepository userRepository;
  private final IHeroService heroService;
  private final IPartyService partyService;
  private final Random random = new Random();

  @Override
  public boolean hasActiveCampaign(Long userId) {
    return campaignRepository.existsActiveCampaignByOwnerId(userId);
  }

  @Override
  public Campaign getActiveCampaign(String username) {
    return campaignRepository
        .findActiveCampaignByUsername(username)
        .orElseThrow(() -> new RuntimeException("No active campaign found"));
  }

  @Override
  public RoomType enterNextRoom(String username) {

    Campaign campaign = getActiveCampaign(username);

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

  @Override
  public Campaign exitCampaign(String username) {

    Campaign campaign = getActiveCampaign(username);
    return campaignRepository.save(campaign);
  }

  @Override
  public int calculateScore(Campaign campaign) {

    int heroScore =
        campaign.getParty().getHeroes().stream().mapToInt(h -> h.getLevel() * 100).sum();
    int goldScore = campaign.getParty().getGold() * 10;
    int score = heroScore + goldScore;

    campaign.setScore(score);
    campaignRepository.save(campaign);
    return score;
  }

  @Override
  public Campaign startNewCampaign(String username, String heroName, HeroClass heroClass) {

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    if (hasActiveCampaign(user.getId())) throw new RuntimeException("Campaign already in progress");

    Party party = partyService.createPartyForUser(user.getId());
    heroService.createBaseHeroForParty(party.getId(), heroName, heroClass);

    Campaign campaign =
        Campaign.builder().owner(user).active(true).currentRoom(0).party(party).build();

    return campaignRepository.save(campaign);
  }

  @Override
  public List<ProfileInfo.CampaignResultInfo> getCampaignResultsForUser(Long userId) {
    return campaignRepository.findAllByOwnerIdOrderByScoreDesc(userId).stream()
        .map(
            c -> new ProfileInfo.CampaignResultInfo(c.getScore(), c.getCurrentRoom(), c.isActive()))
        .toList();
  }

  @Override
  public void savePartyFromCampaign(Long campaignId, Long userId) {

    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    long savedCount = user.getParties().stream().filter(Party::isSaved).count();

    if (savedCount >= 5)
      throw new RuntimeException("Already have 5 saved parties - replace one first");

    Campaign campaign =
        campaignRepository
            .findById(campaignId)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));

    campaign.getParty().setSaved(true);
    campaign.setActive(false);
    campaignRepository.save(campaign);
  }

  @Override
  public void replacePartyFromCampaign(Long campaignId, Long userId, Long partyIdToReplace) {

    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    Party toReplace =
        user.getParties().stream()
            .filter(p -> p.getId().equals(partyIdToReplace) && p.isSaved())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Saved party not found"));

    user.getParties().remove(toReplace);
    partyService.deleteParty(toReplace.getId());
    userRepository.save(user);

    savePartyFromCampaign(campaignId, userId);
  }

  @Override
  public Campaign completeCampaign(String username) {

    Campaign campaign = getActiveCampaign(username);
    calculateScore(campaign);
    campaign.setActive(false);
    return campaignRepository.save(campaign);
  }

  public boolean isCampaignComplete(String username) {

    Campaign campaign = getActiveCampaign(username);
    return campaign.getCurrentRoom() >= 30;
  }

  @Override
  public Campaign getMostRecentCompletedCampaign(String username) {

    return campaignRepository.findCompletedByOwnerUsername(username).stream()
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No completed campaign found"));
  }

  @Override
  public CompleteCampaignInfo getCompletionData(String username) {

    Campaign campaign = getMostRecentCompletedCampaign(username);

    List<Party> savedParties =
        campaign.getOwner().getParties().stream().filter(Party::isSaved).toList();

    boolean partyFull = savedParties.size() >= 5;

    return new CompleteCampaignInfo(
        campaign.getId(),
        campaign.getScore(),
        campaign.getParty().getGold(),
        campaign.getParty().getHeroes(),
        partyFull,
        savedParties);
  }

  @Override
  public CampaignViewInfo getCampaignViewData(String username) {

    Campaign campaign = getActiveCampaign(username);

    return new CampaignViewInfo(
        campaign.getId(),
        campaign.getCurrentRoom(),
        campaign.getParty().getGold(),
        campaign.getParty().getHeroes());
  }
}
