package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.dto.response.ProfileInfo;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.PartyRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.ICampaignService;
import com.university.project.legendsofswordandwand.service.IHeroService;
import com.university.project.legendsofswordandwand.service.IPartyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Campaign Object Service class. */
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements ICampaignService {

  private final CampaignRepository campaignRepository;
  private final UserRepository userRepository;
  private final IHeroService heroService;
  private final IPartyService partyService;
  private final PartyRepository partyRepository;

  @Override
  public boolean hasActiveCampaign(Long userId) {
    return campaignRepository.existsActiveCampaignByOwnerId(userId);
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
        Campaign.builder().owner(user).active(true).currentRoom(1).party(party).build();

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
    partyRepository.delete(toReplace);
    userRepository.save(user);

    savePartyFromCampaign(campaignId, userId);
  }
}
