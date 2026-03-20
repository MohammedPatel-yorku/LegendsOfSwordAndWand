package com.university.project.legendsofswordandwand.battle.ability.chaos;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.battle.ability.AbilityHelper;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.util.ArrayList;
import java.util.List;

public class FireballAbility implements Ability {

  private final HybridClass hybridClass;

  public FireballAbility(HybridClass hybridClass) {
    this.hybridClass = hybridClass;
  }

  @Override
  public int getManaCost() {
    return 30;
  }

  @Override
  public void execute(
      BattleUnit caster,
      BattleUnit target,
      List<BattleUnit> allies,
      List<BattleUnit> enemies,
      BattleState state) {

    double multiplier = (hybridClass == HybridClass.SORCERER) ? 2.0 : 1.0;

    List<BattleUnit> hits;
    if (target == null) {
      hits = enemies.stream().limit(3).toList();
    } else {
      hits = new ArrayList<>();
      if (enemies.contains(target)) hits.add(target);
      for (BattleUnit unit : enemies) {
        if (hits.size() >= 3) break;
        if (unit.getBattleId() != target.getBattleId()) hits.add(unit);
      }
    }

    for (BattleUnit hit : hits) {
      int damage = (int) (AbilityHelper.calculateDamage(caster, hit) * multiplier);
      int hpBefore = hit.getHero().getHealth();
      AbilityHelper.applyDamage(caster.getHero(), hit, damage, state);
      int actual = hpBefore - hit.getHero().getHealth();

      state.log(
          "  🔥 Fireball hits "
              + hit.getHero().getName()
              + " for "
              + actual
              + " dmg → "
              + hit.getHero().getHealth()
              + " HP left");
    }
  }
}
