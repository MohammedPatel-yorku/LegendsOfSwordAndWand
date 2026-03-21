package com.university.project.legendsofswordandwand.battle;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Spring component responsible for calculating and applying stat changes to a {@link Hero} upon
 * levelling up or class promotion.
 *
 * <p>Delegates class-specific stat bonuses to the appropriate {@link ClassBonusStrategy}, and
 * resolves hybrid class assignments via {@link HybridClassResolver}.
 */
@Component
@RequiredArgsConstructor
public class HeroStatCalculator {

  private final List<ClassBonusStrategy> strategies;

  /** The resolver used to determine a hero's hybrid class upon promotion. */
  @Getter private final HybridClassResolver hybridClassResolver;

  /**
   * Applies a full level-up to the given hero, including base stat gains and class bonuses.
   *
   * <ul>
   *   <li>Hybrid heroes receive one bonus application from each of their two classes.
   *   <li>Heroes whose primary class matches {@code heroClass} receive the class bonus twice.
   *   <li>All other heroes receive the class bonus once.
   * </ul>
   *
   * <p>The experience threshold for the next level is recalculated and set after stat gains.
   *
   * @param hero the {@link Hero} to level up
   * @param heroClass the {@link HeroClass} context for determining which bonus to apply
   */
  public void applyLevelUp(Hero hero, HeroClass heroClass) {

    applyBaseGain(hero);

    if (hero.isHybrid()) {
      applyClassBonus(hero, hero.getPrimaryClass());
      applyClassBonus(hero, hero.getSecondaryClass());
    } else if (hero.getPrimaryClass() != null && hero.getPrimaryClass() == heroClass) {
      applyClassBonus(hero, heroClass);
      applyClassBonus(hero, heroClass);
    } else {
      applyClassBonus(hero, heroClass);
    }

    hero.setExperienceToNextLevel(calculateExpThreshold(hero.getLevel()));
  }

  /**
   * Applies only the class bonus for the given {@link HeroClass} to the hero, without incrementing
   * their level or applying base stat gains.
   *
   * @param hero the {@link Hero} to apply the bonus to
   * @param heroClass the {@link HeroClass} whose bonus strategy to apply
   */
  public void applyClassBonusOnly(Hero hero, HeroClass heroClass) {

    applyClassBonus(hero, heroClass);
  }

  /**
   * Calculates the cumulative experience required to reach the given level.
   *
   * <p>The threshold grows quadratically: each level adds {@code 500 + 75*level + 20*level²} to the
   * previous threshold.
   *
   * @param level the target level
   * @return the total experience required to reach {@code level}
   */
  private int calculateExpThreshold(int level) {
    if (level <= 1) return 500 + 75 + 20; // Exp(1) = 0 + 500 + 75*1 + 20*1 = 595
    return calculateExpThreshold(level - 1) + 500 + 75 * level + 20 * level * level;
  }

  /**
   * Applies the base stat gains for a level-up, incrementing level, attack, defense, health, max
   * health, mana, and max mana by fixed amounts.
   *
   * @param hero the {@link Hero} to apply base gains to
   */
  private void applyBaseGain(Hero hero) {

    hero.setLevel(hero.getLevel() + 1);
    hero.setAttack(hero.getAttack() + 1);
    hero.setDefense(hero.getDefense() + 1);
    hero.setHealth(hero.getHealth() + 5);
    hero.setMaxHealth(hero.getMaxHealth() + 5);
    hero.setMana(hero.getMana() + 2);
    hero.setMaxMana(hero.getMaxMana() + 2);
  }

  /**
   * Applies the level bonus for the given {@link HeroClass} to the hero by delegating to the
   * matching {@link ClassBonusStrategy}.
   *
   * @param hero the {@link Hero} to apply the bonus to
   * @param heroClass the {@link HeroClass} whose strategy to use
   */
  private void applyClassBonus(Hero hero, HeroClass heroClass) {

    getStrategy(heroClass).applyLevelBonus(hero);
  }

  /**
   * Retrieves the {@link ClassBonusStrategy} registered for the given {@link HeroClass}.
   *
   * @param heroClass the {@link HeroClass} to look up
   * @return the matching {@link ClassBonusStrategy}
   * @throws IllegalArgumentException if no strategy is registered for the given class
   */
  private ClassBonusStrategy getStrategy(HeroClass heroClass) {

    return strategies.stream()
        .filter(s -> s.getHeroClass() == heroClass)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No strategy found for " + heroClass));
  }
}
