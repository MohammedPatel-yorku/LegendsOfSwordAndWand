package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.battle.ability.Ability;
import com.university.project.legendsofswordandwand.battle.ability.AbilityFactory;
import com.university.project.legendsofswordandwand.battle.ability.AbilityHelper;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import com.university.project.legendsofswordandwand.model.enums.HybridClass;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Spring component responsible for executing hero abilities and applying passive combat effects
 * during battle.
 *
 * <p>Delegates ability construction to {@link AbilityFactory} and damage application to {@link
 * AbilityHelper}.
 */
@Component
@RequiredArgsConstructor
public class AbilityExecutor {

  private final AbilityFactory abilityFactory;
  private final Random random = new Random();

  /**
   * Resolves and executes an ability for the given caster.
   *
   * <p>The caster's effective {@link HeroClass} is determined by their primary class if set,
   * otherwise falling back to their starting class. The resolved {@link Ability} is then executed
   * after deducting its mana cost from the caster.
   *
   * @param caster the {@link BattleUnit} casting the ability
   * @param target the primary target {@link BattleUnit}, may be {@code null} for untargeted
   *     abilities
   * @param allies the list of allied {@link BattleUnit}s
   * @param enemies the list of enemy {@link BattleUnit}s
   * @param state the current {@link BattleState}
   * @param abilityIndex the ability slot index ({@code 0} for the first ability, {@code 1} for the
   *     second)
   * @throws IllegalStateException if the caster does not have enough mana to cast the ability
   */
  public void executeAbility(
      BattleUnit caster,
      BattleUnit target,
      List<BattleUnit> allies,
      List<BattleUnit> enemies,
      BattleState state,
      int abilityIndex) {

    HeroClass effectiveClass =
        (caster.getHero().getPrimaryClass() != null)
            ? caster.getHero().getPrimaryClass()
            : caster.getHero().getStartingClass();
    HybridClass hybridClass = caster.getHero().getHybridClass();

    Ability ability = abilityFactory.resolve(effectiveClass, hybridClass, abilityIndex);

    if (caster.getHero().getMana() < ability.getManaCost())
      throw new IllegalStateException(caster.getHero().getName() + " does not have enough mana");

    caster.getHero().setMana(caster.getHero().getMana() - ability.getManaCost());
    ability.execute(caster, target, allies, enemies, state);
  }

  /**
   * Randomly applies a sneak attack from the attacker to the defender.
   *
   * <p>Has a 50% chance of triggering. If it triggers, deals bonus damage equal to half of the
   * standard calculated damage between the two units.
   *
   * @param attacker the {@link BattleUnit} attempting the sneak attack
   * @param defender the {@link BattleUnit} receiving the potential bonus damage
   * @param state the current {@link BattleState}, used for damage application
   */
  public void maybeSneak(BattleUnit attacker, BattleUnit defender, BattleState state) {

    if (random.nextBoolean()) {
      int bonus = AbilityHelper.calculateDamage(attacker, defender) / 2;
      AbilityHelper.applyDamage(attacker.getHero(), defender, bonus, state);
    }
  }

  /**
   * Applies a mana burn to the defender, draining 10% of their maximum mana.
   *
   * <p>The defender's mana is reduced by 10% of their maximum mana, floored at {@code 0}.
   *
   * @param defender the {@link BattleUnit} whose mana is to be burned
   */
  public void applyManaBurn(BattleUnit defender) {

    int burn = (int) (defender.getHero().getMaxMana() * 0.10);
    defender.getHero().setMana(Math.max(0, defender.getHero().getMana() - burn));
  }
}
