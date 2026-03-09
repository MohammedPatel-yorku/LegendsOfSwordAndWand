package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.dto.ProfileInfo;
import com.university.project.legendsofswordandwand.dto.ProfileInfo.*;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.model.User;
import com.university.project.legendsofswordandwand.repository.CampaignRepository;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final UserRepository userRepository;
  private final CampaignRepository campaignRepository;

  public ProfileInfo getProfile(String username) {

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    List<PartyInfo> partyInfoList = user.getParties().stream().map(this::toPartyInfo).toList();

    List<CampaignResultInfo> campaignResults =
        campaignRepository.findAllByOwnerIdOrderByScoreDesc(user.getId()).stream()
            .map(c -> new CampaignResultInfo(c.getScore(), c.getCurrentRoom(), c.isActive()))
            .toList();

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
