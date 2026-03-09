package com.university.project.legendsofswordandwand.service;

import com.university.project.legendsofswordandwand.model.Party;

public interface IPartyService {

  Party createPartyForUser(Long userId);

  Party getActiveParty(Long campaignId);

  Party reviveAndHealParty(Long campaignId);

  Party recruitHero(Long campaignId, Long heroId);
}
