package com.university.project.legendsofswordandwand.service.party;

import com.university.project.legendsofswordandwand.model.Party;

public interface IPartyService {

  Party createPartyForUser(Long userId);

  void deleteParty(Long partyId);
}
