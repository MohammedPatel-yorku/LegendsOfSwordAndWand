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

      if (state.getShield(ally.getBattleId()) == 0) {
        state.setShield(ally.getBattleId(), -shield);
        state.log(
            "  🔥 Fire Shield shields " + ally.getHero().getName() + " for " + shield + " HP");
      } else {
        state.log(
            "  🔥 Fire Shield has no effect — "
                + ally.getHero().getName()
                + " is already shielded");
      }
    }
  }
}
