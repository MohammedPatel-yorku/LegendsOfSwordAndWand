package com.university.project.legendsofswordandwand.battle.ability.order;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import java.util.List;

/**
 * An ability that wraps all allies in a fire shield, absorbing a portion of incoming damage.
 *
 * <p>Each ally without an existing shield receives a shield worth 10% of their maximum health.
 * Allies that are already shielded are unaffected.
 */
public class FireShieldAbility implements Ability {

  /**
   * Returns the mana cost of casting Fire Shield.
   *
   * @return {@code 25}
   */
  @Override
  public int getManaCost() {
    return 25;
  }

  /**
   * Executes the Fire Shield ability, applying a damage-absorbing shield to all unshielded allies.
   *
   * <p>For each ally, if no shield is currently active, a shield equal to 10% of that ally's
   * maximum health is applied via {@link BattleState#setShield}. If the ally already has a shield,
   * the ability has no effect on them. Each outcome is logged to the {@link BattleState}.
   *
   * @param caster the {@link BattleUnit} casting the ability (unused by this ability)
   * @param target the intended target (unused by this ability)
   * @param allies the list of allied {@link BattleUnit}s to potentially shield
   * @param enemies the list of enemy {@link BattleUnit}s (unused by this ability)
   * @param state the current {@link BattleState}, used for shield tracking and logging
   */
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
