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

/**
 * An ability that strikes a primary target with lightning, then chains to all remaining enemies
 * with exponentially reduced damage.
 *
 * <p>The falloff multiplier applied to each successive chain hit depends on the caster's {@link
 * HybridClass}: {@code INVOKER} retains 50% damage per bounce, while all other classes retain only
 * 25%.
 */
public class ChainLightningAbility implements Ability {

  private final HybridClass hybridClass;
  private final Random random;

  /**
   * Constructs a {@code ChainLightningAbility} for the given hybrid class.
   *
   * @param hybridClass the caster's hybrid class, used to determine the damage falloff rate
   * @param random the {@link Random} instance used to shuffle the chain-bounce order
   */
  public ChainLightningAbility(HybridClass hybridClass, Random random) {
    this.hybridClass = hybridClass;
    this.random = random;
  }

  /**
   * Returns the mana cost of casting Chain Lightning.
   *
   * @return {@code 40}
   */
  @Override
  public int getManaCost() {
    return 40;
  }

  /**
   * Executes the Chain Lightning ability.
   *
   * <p>Deals full calculated damage to {@code target}, then bounces to all other enemies in a
   * random order. Each successive bounce deals the previous hit's damage multiplied by the falloff
   * rate (0.50 for {@code INVOKER}, 0.25 otherwise), with a minimum of 1 damage per hit. Each hit
   * is logged to the {@link BattleState}.
   *
   * <p>Does nothing if {@code target} is {@code null} or {@code enemies} is empty.
   *
   * @param caster the {@link BattleUnit} casting the ability
   * @param target the primary {@link BattleUnit} to strike first
   * @param allies the list of allied {@link BattleUnit}s (unused by this ability)
   * @param enemies the list of all enemy {@link BattleUnit}s, including {@code target}
   * @param state the current {@link BattleState}, used for damage application and logging
   */
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
      int chainDmg = Math.max(1, (int) current);
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
