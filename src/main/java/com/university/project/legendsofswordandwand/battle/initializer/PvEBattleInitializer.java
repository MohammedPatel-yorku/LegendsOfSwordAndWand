package com.university.project.legendsofswordandwand.battle.initializer;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.battle.enemy.EnemyGenerator;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.service.party.IPartyManagementService;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete {@link BattleInitializer} for Player vs Environment battles.
 *
 * <p>Builds the player side from the active campaign party, excluding any temporary (unchosen
 * recruit) heroes. Builds the enemy side by delegating to {@link EnemyGenerator}, which scales the
 * enemy party to the player's cumulative level. Enemy units are assigned negative battle IDs to
 * avoid collisions with player unit IDs, and each enemy unit is constructed with its
 * {@link com.university.project.legendsofswordandwand.battle.enemy.EnemyBehaviour} archetype — set once
 * at battle initialisation so the battle service never needs to re-derive it from a name string.
 *
 * <p>On battle end, stores the campaign ID back into the {@link BattleState} so downstream
 * services can identify which campaign the battle belonged to.
 */
public class PvEBattleInitializer extends BattleInitializer {

  private final Long campaignId;
  private final int playerCumulativeLevel;
  private final IPartyManagementService partyManagementService;
  private final EnemyGenerator enemyGenerator;

  /**
   * Constructs a {@code PvEBattleInitializer} for the given campaign.
   *
   * @param campaignId the ID of the active campaign this battle belongs to
   * @param playerCumulativeLevel the sum of all player hero levels, used to scale enemies
   * @param partyManagementService service used to retrieve the active party for the campaign
   * @param enemyGenerator generates a scaled enemy party for the battle
   */
  public PvEBattleInitializer(
      Long campaignId,
      int playerCumulativeLevel,
      IPartyManagementService partyManagementService,
      EnemyGenerator enemyGenerator) {

    this.campaignId = campaignId;
    this.playerCumulativeLevel = playerCumulativeLevel;
    this.partyManagementService = partyManagementService;
    this.enemyGenerator = enemyGenerator;
  }

  /**
   * Builds the player-side {@link BattleUnit} list from the active campaign party.
   *
   * <p>Only non-temporary heroes are included. Each hero is wrapped in a {@link HeroSnapshot} to
   * decouple battle stat mutations from the persistent entity.
   *
   * @return a list of player-controlled {@link BattleUnit}s
   */
  @Override
  protected List<BattleUnit> buildPlayerUnits() {
    Party party = partyManagementService.getActiveParty(campaignId);
    List<BattleUnit> units = new ArrayList<>();
    for (var hero : party.getHeroes()) {
      if (!hero.isTemporary())
        units.add(new BattleUnit(hero.getId(), new HeroSnapshot(hero), false));
    }
    return units;
  }

  /**
   * Builds the enemy-side {@link BattleUnit} list by generating a scaled enemy party.
   *
   * <p>Delegates to {@link EnemyGenerator#generate}, which returns {@link EnemyGenerator.EnemyEntry}
   * records pairing each enemy hero with its {@link com.university.project.legendsofswordandwand.battle.enemy.EnemyBehaviour}.
   * The behavior is passed directly to the {@link BattleUnit} constructor so it is available to
   * the combat AI without any string matching at runtime.
   *
   * <p>Enemy units are assigned sequential negative battle IDs starting at {@code -1} to avoid
   * collisions with player unit IDs.
   *
   * @return a list of enemy {@link BattleUnit}s, each carrying its AI behavior archetype
   */
  @Override
  protected List<BattleUnit> buildEnemyUnits() {
    Party party = partyManagementService.getActiveParty(campaignId);
    List<EnemyGenerator.EnemyEntry> entries =
            enemyGenerator.generate(playerCumulativeLevel, party.getHeroes().size());

    List<BattleUnit> units = new ArrayList<>();
    long enemyId = -1L;
    for (EnemyGenerator.EnemyEntry entry : entries) {
      units.add(
              new BattleUnit(enemyId--, new HeroSnapshot(entry.hero()), true, entry.behaviour()));
    }
    return units;
  }

  /**
   * Stores the campaign ID into the {@link BattleState} after the battle ends.
   *
   * <p>This allows {@code BattleServiceImpl} and {@code BattleController} to identify which
   * campaign the completed battle belongs to when distributing rewards or clearing the pending
   * room flag.
   *
   * @param state the completed {@link BattleState}
   */
  @Override
  public void onBattleEnd(BattleState state) {
    state.setCampaignId(campaignId);
  }
}
