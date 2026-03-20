package com.university.project.legendsofswordandwand.battle.ability.order;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import java.util.List;

public class FireShieldAbility implements Ability {

  @Override
  public int getManaCost() {
    return 25;
  }

  @Override
  public void execute(
      BattleUnit caster,
      BattleUnit target,
      List<BattleUnit> allies,
      List<BattleUnit> enemies,
      BattleState state) {

    for (BattleUnit ally : allies) {

      int shield = (int) (ally.getHero().getMaxHealth() * 0.10);
      int current = Math.abs(state.getShield(ally.getBattleId()));

      state.setShield(ally.getBattleId(), -(Math.max(current, shield)));
    }
  }
}
