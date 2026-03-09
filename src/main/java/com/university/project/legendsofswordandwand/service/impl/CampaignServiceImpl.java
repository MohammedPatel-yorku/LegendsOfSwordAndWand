package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.dto.ProfileInfo;
import com.university.project.legendsofswordandwand.model.Campaign;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
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
}
