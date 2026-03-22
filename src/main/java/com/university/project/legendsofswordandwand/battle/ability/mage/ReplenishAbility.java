package com.university.project.legendsofswordandwand.battle.ability.mage;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.util.List;

/**
 * An ability that restores mana to all allies, with a larger bonus for the caster.
 *
 * <p>The {@code PROPHET} hybrid class doubles all restore amounts. The {@code WIZARD} hybrid class
 * benefits from a reduced mana cost to cast this ability.
 */
public class ReplenishAbility implements Ability {

  private final HybridClass hybridClass;

  /**
   * Constructs a {@code ReplenishAbility} for the given hybrid class.
   *
   * @param hybridClass the caster's hybrid class, used to determine mana cost and restore amounts
   */
  public ReplenishAbility(HybridClass hybridClass) {
    this.hybridClass = hybridClass;
  }

  /**
   * Returns the mana cost of casting Replenish.
   *
   * <p>Costs {@code 40} for {@code WIZARD}, or {@code 80} for all other classes.
   *
   * @return the mana cost of this ability
   */
  @Override
  public int getManaCost() {
    return (hybridClass == HybridClass.WIZARD) ? 40 : 80;
  }

  /**
   * Executes the Replenish ability, restoring mana to all allies.
   *
   * <p>Each ally receives {@code 30} mana, while the caster receives {@code 60} mana. Both values
   * are doubled if the caster's {@link HybridClass} is {@code PROPHET}. Mana is capped at each
   * hero's maximum. Each restore is logged to the {@link BattleState}.
   *
   * @param caster the {@link BattleUnit} casting the ability, who receives the larger self-restore
   * @param target the intended target (unused by this ability)
   * @param allies the list of allied {@link BattleUnit}s to restore mana to, including the caster
   * @param enemies the list of enemy {@link BattleUnit}s (unused by this ability)
   * @param state the current {@link BattleState}, used for logging
   */
  @Override
  public void execute(
      BattleUnit caster,
      BattleUnit target,
      List<BattleUnit> allies,
      List<BattleUnit> enemies,
      BattleState state) {

    double multiplier = (hybridClass == HybridClass.PROPHET) ? 2.0 : 1.0;
    int allyRestore = (int) (30 * multiplier);
    int selfRestore = (int) (60 * multiplier);

    for (BattleUnit ally : allies) {

      HeroSnapshot hero = ally.getHero();
      int restore = (ally.getBattleId() == caster.getBattleId()) ? selfRestore : allyRestore;
      hero.setMana(Math.min(hero.getMaxMana(), hero.getMana() + restore));
      state.log(
          "  ✦ Replenish restores "
              + restore
              + " MP to "
              + hero.getName()
              + " → "
              + hero.getMana()
              + " MP");
    }
  }
}
