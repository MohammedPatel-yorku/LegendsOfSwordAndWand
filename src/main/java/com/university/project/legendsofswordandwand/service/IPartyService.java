package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Party;

public interface IPartyService {

  Party createPartyForUser(Long userId);

  Party getActiveParty(Long campaignId);

  Party reviveAndHealParty(Long campaignId);

  void deleteParty(Long partyId);

  void updateGold(Long partyId, int cost);

  void addHeroToParty(Long partyId, Long heroId);
}
