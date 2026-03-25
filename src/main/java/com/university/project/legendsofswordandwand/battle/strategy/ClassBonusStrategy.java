package com.university.project.legendsofswordandwand.battle.strategy;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;

/**
 * Strategy interface for applying per-level stat bonuses based on a hero's class.
 *
 * <p>Each implementation encapsulates the stat growth for one {@link HeroClass}, keeping
 * class-specific bonus logic out of {@code HeroStatCalculator} and making it easy to add new
 * classes without modifying existing code.
 *
 * <p>Implementations are registered as Spring {@code @Component}s and injected as a list into
 * {@code HeroStatCalculator}, which selects the correct strategy at runtime by matching on {@link
 * #getHeroClass()}.
 */
public interface ClassBonusStrategy {

  /**
   * Returns the {@link HeroClass} this strategy applies to.
   *
   * @return the hero class handled by this strategy
   */
  HeroClass getHeroClass();

  /**
   * Applies the per-level stat bonus for this class to the given hero.
   *
   * <p>Modifies the hero's stats directly. Called by {@code HeroStatCalculator} once per class
   * level gained, or twice when the hero's primary class matches the levelled class (specialisation
   * double bonus).
   *
   * @param hero the {@link Hero} to apply the bonus to
   */
  void applyLevelBonus(Hero hero);
}
