package com.university.project.legendsofswordandwand.battle.initializer;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.EnemyGenerator;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.Party;
import com.university.project.legendsofswordandwand.service.party.IPartyManagementService;
import java.util.ArrayList;
import java.util.List;

public class PvEBattleInitializer extends BattleInitializer {

  private final Long campaignId;
  private final int playerCumulativeLevel;
  private final IPartyManagementService partyManagementService;
  private final EnemyGenerator enemyGenerator;

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

  @Override
  protected List<BattleUnit> buildPlayerUnits() {

    Party party = partyManagementService.getActiveParty(campaignId);
    List<BattleUnit> units = new ArrayList<>();

    for (Hero hero : party.getHeroes()) {
      if (!hero.isTemporary())
        units.add(new BattleUnit(hero.getId(), new HeroSnapshot(hero), false));
    }
    return units;
  }

  @Override
  protected List<BattleUnit> buildEnemyUnits() {

    Party party = partyManagementService.getActiveParty(campaignId);
    List<Hero> enemies = enemyGenerator.generate(playerCumulativeLevel, party.getHeroes().size());
    List<BattleUnit> units = new ArrayList<>();

    long enemyId = -1L;
    for (Hero enemy : enemies) units.add(new BattleUnit(enemyId--, new HeroSnapshot(enemy), true));
    return units;
  }

  @Override
  public void onBattleEnd(BattleState state) {

    state.setCampaignId(campaignId);
  }
}
