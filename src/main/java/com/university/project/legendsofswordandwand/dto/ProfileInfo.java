package com.university.project.legendsofswordandwand.dto;

import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import java.util.List;

public record ProfileInfo(
    String username,
    int pvpWins,
    int pvpLosses,
    List<PartyInfo> parties,
    List<CampaignResultInfo> campaignResults) {

  public record PartyInfo(Long id, int gold, int cumulativeLevel, List<HeroInfo> heroes) {}

  public record HeroInfo(
      String name, HeroClass heroClass, int level, int health, int attack, int defense, int mana) {}

  public record CampaignResultInfo(int score, int roomsReached, boolean active) {}
}
