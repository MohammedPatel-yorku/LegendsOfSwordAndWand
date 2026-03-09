package com.university.project.legendsofswordandwand.service.impl;

import com.university.project.legendsofswordandwand.dto.ProfileInfo;
import com.university.project.legendsofswordandwand.dto.ProfileInfo.*;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import com.university.project.legendsofswordandwand.service.ICampaignService;
import com.university.project.legendsofswordandwand.service.IProfileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements IProfileService {

  private final UserRepository userRepository;
  private final ICampaignService campaignService;

  @Override
  public ProfileInfo getProfile(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    List<PartyInfo> partyInfoList = user.getParties().stream().map(this::toPartyInfo).toList();

    List<CampaignResultInfo> campaignResults =
        campaignService.getCampaignResultsForUser(user.getId()); // delegated to campaign service

    return new ProfileInfo(
        user.getUsername(), user.getPvpWins(), user.getPvpLosses(), partyInfoList, campaignResults);
  }

  private PartyInfo toPartyInfo(Party party) {
    List<HeroInfo> heroInfoList = party.getHeroes().stream().map(this::toHeroInfo).toList();
    return new PartyInfo(party.getId(), party.getGold(), party.getCumulativeLevel(), heroInfoList);
  }

  private HeroInfo toHeroInfo(Hero hero) {
    return new HeroInfo(
        hero.getName(),
        hero.getHeroClass(),
        hero.getLevel(),
        hero.getHealth(),
        hero.getAttack(),
        hero.getDefense(),
        hero.getMana());
  }
}
