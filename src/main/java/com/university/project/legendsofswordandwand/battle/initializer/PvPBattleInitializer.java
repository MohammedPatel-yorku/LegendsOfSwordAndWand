package com.university.project.legendsofswordandwand.battle.initializer;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete {@link BattleInitializer} for Player vs Player battles.
 *
 * <p>Builds both sides from pre-saved parties: the sender's party becomes the player side and the
 * receiver's party becomes the enemy side. Both parties are fully restored to maximum HP and mana
 * before the battle begins, ensuring PvP fights are not influenced by prior campaign state.
 *
 * <p>On battle end, the win and loss records of both players are updated in the database via {@link
 * UserRepository}.
 */
public class PvPBattleInitializer extends BattleInitializer {

  private final Party senderParty;
  private final Party receiverParty;
  private final UserRepository userRepository;

  /**
   * Constructs a {@code PvPBattleInitializer} for the given parties and invitation.
   *
   * <p>Both parties are immediately restored to full HP and mana upon construction, before {@link
   * #initialize()} is called.
   *
   * @param senderParty the party belonging to the player who sent the invitation
   * @param receiverParty the party belonging to the player who accepted the invitation
   * @param invitationId the ID of the {@code PvPInvitation} that triggered this battle
   * @param userRepository used to persist win/loss stat updates after the battle ends
   */
  public PvPBattleInitializer(
      Party senderParty, Party receiverParty, Long invitationId, UserRepository userRepository) {

    this.senderParty = senderParty;
    this.receiverParty = receiverParty;
    this.userRepository = userRepository;

    restoreParty(senderParty);
    restoreParty(receiverParty);
  }

  /**
   * Builds the player-side {@link BattleUnit} list from the sender's party.
   *
   * <p>Only non-temporary heroes are included. Each hero is wrapped in a {@link HeroSnapshot}.
   * Battle IDs are assigned sequentially starting at {@code 1}.
   *
   * @return a list of player-controlled {@link BattleUnit}s for the sender
   */
  @Override
  protected List<BattleUnit> buildPlayerUnits() {

    List<BattleUnit> units = new ArrayList<>();
    long id = 1L;

    for (Hero hero : senderParty.getHeroes())
      if (!hero.isTemporary()) units.add(new BattleUnit(id++, new HeroSnapshot(hero), false));
    return units;
  }

  /**
   * Builds the enemy-side {@link BattleUnit} list from the receiver's party.
   *
   * <p>Only non-temporary heroes are included. Each hero is wrapped in a {@link HeroSnapshot}.
   * Battle IDs are assigned sequential negative values starting at {@code -1} to avoid collisions
   * with player unit IDs.
   *
   * @return a list of enemy {@link BattleUnit}s for the receiver
   */
  @Override
  protected List<BattleUnit> buildEnemyUnits() {

    List<BattleUnit> units = new ArrayList<>();
    long id = -1L;

    for (Hero hero : receiverParty.getHeroes())
      if (!hero.isTemporary()) units.add(new BattleUnit(id--, new HeroSnapshot(hero), true));
    return units;
  }

  /**
   * Updates the win and loss records of both players after the battle concludes.
   *
   * <p>Determines the winner based on {@link BattleState#getStatus()}. The winning player's {@code
   * pvpWins} counter and the losing player's {@code pvpLosses} counter are each incremented by one
   * and persisted via {@link UserRepository}.
   *
   * @param state the completed {@link BattleState}
   */
  @Override
  public void onBattleEnd(BattleState state) {

    boolean senderWon =
        state.getStatus()
            == com.university.project.legendsofswordandwand.model.enums.BattleStatus.PLAYER_WIN;

    String winnerUsername =
        senderWon ? senderParty.getOwner().getUsername() : receiverParty.getOwner().getUsername();
    String loserUsername =
        senderWon ? receiverParty.getOwner().getUsername() : senderParty.getOwner().getUsername();

    userRepository
        .findByUsername(winnerUsername)
        .ifPresent(
            u -> {
              u.setPvpWins(u.getPvpWins() + 1);
              userRepository.save(u);
            });
    userRepository
        .findByUsername(loserUsername)
        .ifPresent(
            u -> {
              u.setPvpLosses(u.getPvpLosses() + 1);
              userRepository.save(u);
            });
  }

  /**
   * Restores all non-temporary heroes in the given party to full HP and mana.
   *
   * <p>Called in the constructor for both parties before the battle starts, ensuring PvP fights
   * always begin from a clean state regardless of how the heroes were left after their last
   * campaign.
   *
   * @param party the {@link Party} whose heroes should be restored
   */
  private void restoreParty(Party party) {
    party.getHeroes().stream()
        .filter(h -> !h.isTemporary())
        .forEach(
            h -> {
              h.setHealth(h.getMaxHealth());
              h.setMana(h.getMaxMana());
            });
  }
}
