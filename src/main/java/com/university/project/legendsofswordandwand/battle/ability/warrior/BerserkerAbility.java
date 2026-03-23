package com.university.project.legendsofswordandwand.battle.ability.warrior;

import com.university.project.legendsofswordandwand.battle.BattleState;
import com.university.project.legendsofswordandwand.battle.BattleUnit;
import com.university.project.legendsofswordandwand.battle.HeroSnapshot;
import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.battle.ability.AbilityHelper;
import java.util.List;

/**
 * Base implementation of the Berserker Attack ability for the {@code WARRIOR} class.
 *
 * <p>Deals full damage to a primary target and splash damage equal to 25% of the primary damage to
 * up to two additional enemies. This class contains only the core attack behaviour — hybrid class
 * modifications (stun for {@code KNIGHT}, self-heal for {@code PALADIN}) are applied externally via
 * the Decorator pattern by {@code AbilityFactory}.
 *
 * @see com.university.project.legendsofswordandwand.battle.ability.decorator.StunDecorator
 * @see
 *     com.university.project.legendsofswordandwand.battle.ability.decorator.SelfHealBeforeAttackDecorator
 */
public class BerserkerAbility implements Ability {

  /** Constructs a {@code BerserkerAbility}. */
  public BerserkerAbility() {}

  /**
   * Returns the mana cost of the Berserker Attack.
   *
   * @return {@code 60}
   */
  @Override
  public int getManaCost() {

    return 60;
  }

  /**
   * Executes the Berserker Attack against the primary target and up to two additional enemies.
   *
   * <p>Full damage is dealt to {@code target}. Each of the remaining enemies (up to two, excluding
   * the primary target) receives splash damage equal to 25% of the primary damage. All damage
   * respects the target's defense stat and any active shields via {@link
   * AbilityHelper#applyDamage}. Each hit is logged to the {@link BattleState}.
   *
   * @param caster the {@link BattleUnit} performing the attack
   * @param target the primary target; if {@code null} the ability does nothing
   * @param allies the list of allied {@link BattleUnit}s (unused by this ability)
   * @param enemies the list of enemy {@link BattleUnit}s, used to find splash targets
   * @param state the current {@link BattleState}, used for damage application and logging
   */
  @Override
  public void execute(
      BattleUnit caster,
      BattleUnit target,
      List<BattleUnit> allies,
      List<BattleUnit> enemies,
      BattleState state) {

    if (target == null) return;

    HeroSnapshot casterHero = caster.getHero();
    int primaryDamage = AbilityHelper.calculateDamage(caster, target);
    int hpBefore = target.getHero().getHealth();

    AbilityHelper.applyDamage(casterHero, target, primaryDamage, state);
    int actual = hpBefore - target.getHero().getHealth();
    state.log(
        "  ⚔ Berserker hits "
            + target.getHero().getName()
            + " for "
            + actual
            + " dmg → "
            + target.getHero().getHealth()
            + " HP left");

    enemies.stream()
        .filter(u -> u.getBattleId() != target.getBattleId())
        .limit(2)
        .forEach(
            u -> {
              int before = u.getHero().getHealth();
              AbilityHelper.applyDamage(casterHero, u, primaryDamage / 4, state);
              int splashActual = before - u.getHero().getHealth();
              state.log(
                  "  ⚔ ...splash hits "
                      + u.getHero().getName()
                      + " for "
                      + splashActual
                      + " dmg → "
                      + u.getHero().getHealth()
                      + " HP left");
            });
  }
}
