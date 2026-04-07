package com.university.project.legendsofswordandwand.service.pvp;

import com.university.project.legendsofswordandwand.battle.BattleState;

/** Service interface defining the contract for PvP invitation management. */
public interface IPvPService {

  /**
   * Creates a pending PvP invitation from the sender to the receiver.
   *
   * @param senderUsername the username of the player sending the challenge
   * @param receiverUsername the username of the player being challenged; must have a registered
   *     profile and at least one saved party
   * @throws RuntimeException if either player is not found, the receiver has no saved parties, or a
   *     pending invitation already exists between these players
   */
  void createInvitation(String senderUsername, String receiverUsername);

  /**
   * Marks the given invitation as accepted, making it ready for party selection and battle start.
   *
   * @param inviteId the ID of the {@link
   *     com.university.project.legendsofswordandwand.model.PvPInvitation} to accept
   * @throws RuntimeException if the invitation is not found or is not in {@code PENDING} status
   */
  void acceptInvitation(Long inviteId);

  /**
   * Records the PvP win and loss counts in the database after a PvP battle concludes. Has no effect
   * if the battle is not a PvP battle or has not yet ended.
   *
   * @param state the current {@link BattleState}
   */
  void updatePvPResult(BattleState state);
}
