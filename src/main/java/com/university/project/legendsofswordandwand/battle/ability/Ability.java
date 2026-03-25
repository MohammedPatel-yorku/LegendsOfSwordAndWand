package com.university.project.legendsofswordandwand.battle.ability;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.ability.decorator.AbilityDecorator;
import java.util.List;

/**
 * Core interface representing a hero's special ability in battle.
 *
 * <p>Each ability has a mana cost that is deducted before execution, and an {@link #execute} method
 * that applies its effect to the battle. Abilities are constructed by {@link AbilityFactory}, which
 * selects the correct implementation based on the caster's {@link
 * com.university.project.legendsofswordandwand.model.enums.HeroClass} and {@link
 * com.university.project.legendsofswordandwand.model.enums.HybridClass}.
 *
 * <p>The Decorator pattern is applied through {@link AbilityDecorator}, which implements this
 * interface and wraps other {@code Ability} instances to add hybrid class behaviour without
 * modifying the base ability classes.
 *
 * @see AbilityFactory
 * @see com.university.project.legendsofswordandwand.battle.ability.decorator.AbilityDecorator
 */
public interface Ability {

  /**
   * Returns the mana cost of this ability.
   *
   * <p>This value is checked and deducted from the caster's current mana by {@code AbilityExecutor}
   * before {@link #execute} is called. If the caster does not have enough mana, the cast is
   * rejected.
   *
   * @return the mana cost as a non-negative integer
   */
  int getManaCost();

  /**
   * Executes this ability, applying its effect to the relevant units.
   *
   * <p>Implementations may target a single unit, multiple units, allies, or all combatants
   * depending on the ability. The {@code target} parameter may be {@code null} for untargeted
   * abilities — implementations should handle this gracefully. All damage application should go
   * through {@link
   * com.university.project.legendsofswordandwand.battle.ability.AbilityHelper#applyDamage} to
   * ensure shields are respected. All meaningful events should be logged to {@code state} via
   * {@link com.university.project.legendsofswordandwand.battle.BattleState#log}.
   *
   * @param caster the {@link BattleUnit} casting the ability
   * @param target the primary target {@link BattleUnit}, or {@code null} for untargeted abilities
   * @param allies the list of allied {@link BattleUnit}s available to the caster
   * @param enemies the list of enemy {@link BattleUnit}s available to target
   * @param state the current {@link BattleState}, used for damage application, shield and stun
   *     tracking, and logging
   */
  void execute(
      BattleUnit caster,
      BattleUnit target,
      List<BattleUnit> allies,
      List<BattleUnit> enemies,
      BattleState state);
}
