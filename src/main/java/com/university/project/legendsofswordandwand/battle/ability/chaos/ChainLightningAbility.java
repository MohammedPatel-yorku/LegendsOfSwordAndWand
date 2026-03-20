package com.university.project.legendsofswordandwand.battle.ability.chaos;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.battle.ability.AbilityHelper;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChainLightningAbility implements Ability {

  private final HybridClass hybridClass;
  private final Random random;

  public ChainLightningAbility(HybridClass hybridClass, Random random) {

    this.hybridClass = hybridClass;
    this.random = random;
  }

  @Override
  public int getManaCost() {
    return 40;
  }

  @Override
  public void execute(
      BattleUnit caster,
      BattleUnit target,
      List<BattleUnit> allies,
      List<BattleUnit> enemies,
      BattleState state) {

    if (target == null || enemies.isEmpty()) return;

    double falloff = (hybridClass == HybridClass.INVOKER) ? 0.50 : 0.25;

    int baseDamage = AbilityHelper.calculateDamage(caster, target);
    int hpBefore = target.getHero().getHealth();
    AbilityHelper.applyDamage(caster.getHero(), target, baseDamage, state);
    int actual = hpBefore - target.getHero().getHealth();
    state.log(
        "  ⚡ Chain Lightning hits "
            + target.getHero().getName()
            + " for "
            + actual
            + " dmg → "
            + target.getHero().getHealth()
            + " HP left");

    List<BattleUnit> rest = new ArrayList<>(enemies);
    rest.remove(target);
    Collections.shuffle(rest, random);

    double current = baseDamage;
    for (BattleUnit next : rest) {
      current *= falloff;
      int chainDmg = Math.max(1, (int) current); // always at least 1
      int hpBeforeChain = next.getHero().getHealth();
      AbilityHelper.applyDamage(caster.getHero(), next, chainDmg, state);
      int actualChain = hpBeforeChain - next.getHero().getHealth();
      state.log(
          "  ⚡ ...chains to "
              + next.getHero().getName()
              + " for "
              + actualChain
              + " dmg → "
              + next.getHero().getHealth()
              + " HP left");
    }
  }
}
