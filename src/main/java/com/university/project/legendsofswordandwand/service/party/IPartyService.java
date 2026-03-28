package com.university.project.legendsofswordandwand.service.party;

import com.university.project.legendsofswordandwand.model.Party;

/** Service interface defining the contract for basic party creation and deletion. */
public interface IPartyService {

  /**
   * Creates a new empty party and associates it with the given user.
   *
   * @param userId the ID of the user who owns the party
   * @return the newly created and persisted {@link Party}
   */
  Party createPartyForUser(Long userId);

  /**
   * Permanently deletes the party with the given ID, including all associated heroes.
   *
   * @param partyId the ID of the party to delete
   */
  void deleteParty(Long partyId);
}
