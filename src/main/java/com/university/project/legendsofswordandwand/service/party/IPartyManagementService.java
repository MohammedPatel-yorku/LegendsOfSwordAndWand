package com.university.project.legendsofswordandwand.service.party;

import com.university.project.legendsofswordandwand.model.Party;

import java.util.List;

public interface IPartyManagementService {

  Party getActiveParty(Long campaignId);

  Party reviveAndHealParty(Long campaignId);

  void deductGold(Long partyId, int amount);

  void addGold(Long partyId, int amount);

  void addHeroToParty(Long partyId, Long heroId);

  List<String> reviveAndHealPartyWithSummary(Long campaignId);
}
