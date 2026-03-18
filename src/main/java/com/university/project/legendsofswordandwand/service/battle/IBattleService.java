package com.university.project.legendsofswordandwand.service.battle;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.model.enums.ActionType;
import com.university.project.legendsofswordandwand.model.enums.BattleStatus;

public interface IBattleService {

  BattleState initializePvEBattle(Long campaignId, int playerCumulativeLevel);

  BattleState executePlayerAction(
          BattleState state, ActionType actionType, Long targetBattleId, Integer abilityIndex);

  BattleState executeEnemyTurn(BattleState state);

  BattleStatus checkBattleStatus(BattleState state);

  void awardBattleRewards(BattleState state);

  void applyBattleLoss(BattleState state);
}
