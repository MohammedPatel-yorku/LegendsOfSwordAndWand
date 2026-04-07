package com.university.project.legendsofswordandwand.service.battle;

import com.university.project.legendsofswordandwand.battle.BattleState;
import java.util.Map;

public interface IBattleRewardService {

  /**
   * Awards XP and gold to surviving player heroes after a victory and persists the changes.
   *
   * @param state the current {@link BattleState}
   * @return a map containing {@code "gold"} (int) and {@code "recipients"} (list of reward strings)
   */
  Map<String, Object> awardBattleRewards(BattleState state);

  /**
   * Applies XP and gold penalties to all player heroes after a battle loss.
   *
   * @param state the current {@link BattleState}
   */
  void applyBattleLoss(BattleState state);
}
