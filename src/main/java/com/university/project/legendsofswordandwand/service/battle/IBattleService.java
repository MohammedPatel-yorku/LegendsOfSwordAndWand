package com.university.project.legendsofswordandwand.service.battle;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.model.enums.ActionType;
import com.university.project.legendsofswordandwand.model.enums.BattleStatus;

/**
 * Service interface defining the contract for managing the full lifecycle of a battle, including
 * initialisation, player and enemy actions, status checking, and post-battle rewards.
 */
public interface IBattleService {

  /**
   * Initialises a new PvE battle for the given campaign, generating a scaled enemy party.
   *
   * @param campaignId the ID of the campaign this battle belongs to
   * @param playerCumulativeLevel the sum of all player hero levels, used to scale the enemy party
   * @return the fully initialised {@link BattleState}
   */
  BattleState initializePvEBattle(Long campaignId, int playerCumulativeLevel);

  /**
   * Initialises a new PvP battle between two saved parties.
   *
   * @param senderPartyId the ID of the party belonging to the invitation sender
   * @param receiverPartyId the ID of the party belonging to the invitation receiver
   * @param invitationId the ID of the {@code PvPInvitation} that triggered this battle
   * @return the fully initialised {@link BattleState} in PvP mode
   */
  BattleState initializePvPBattle(Long senderPartyId, Long receiverPartyId, Long invitationId);

  /**
   * Executes the given player action for the currently active unit and advances the turn.
   *
   * @param state the current {@link BattleState}
   * @param actionType the {@link ActionType} the player wishes to perform
   * @param targetBattleId the battle ID of the target unit, or {@code null} if not required
   * @param abilityIndex the ability slot index for {@code CAST} actions, or {@code null}
   * @return the updated {@link BattleState}
   */
  BattleState executePlayerAction(
      BattleState state, ActionType actionType, Long targetBattleId, Integer abilityIndex);

  /**
   * Executes one enemy turn for the currently active enemy unit and advances the turn.
   *
   * @param state the current {@link BattleState}
   * @return the updated {@link BattleState}
   */
  BattleState executeEnemyTurn(BattleState state);

  /**
   * Determines the current {@link BattleStatus} based on surviving units.
   *
   * @param state the current {@link BattleState}
   * @return {@link BattleStatus#PLAYER_WIN}, {@link BattleStatus#PLAYER_LOSE}, or {@link
   *     BattleStatus#IN_PROGRESS}
   */
  BattleStatus checkBattleStatus(BattleState state);

  /**
   * Prepares the battle result display data for the result page and gives any rewards or penalties
   * that have not been processed yet.
   *
   * @param state the current {@link BattleState}
   * @param rewardsAlreadyGiven whether rewards/loss handling has already been applied for this
   *     session
   * @return a {@link BattleResultDTO} containing the display data for the result view
   */
  BattleResultDTO prepareBattleResult(BattleState state, boolean rewardsAlreadyGiven);
}
