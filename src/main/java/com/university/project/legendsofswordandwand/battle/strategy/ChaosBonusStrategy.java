package com.university.project.legendsofswordandwand.battle.strategy;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import org.springframework.stereotype.Component;

/**
 * {@link ClassBonusStrategy} implementation for the {@code CHAOS} hero class.
 *
 * <p>On each level gained in the Chaos class, the hero receives:
 *
 * <ul>
 *   <li>+3 attack
 *   <li>+5 health (current and maximum)
 * </ul>
 */
@Component
public class ChaosBonusStrategy implements ClassBonusStrategy {

  /**
   * Returns {@link HeroClass#CHAOS}.
   *
   * @return {@code HeroClass.CHAOS}
   */
  @Override
  public HeroClass getHeroClass() {

    return HeroClass.CHAOS;
  }

  /**
   * Applies the Chaos class level bonus to the given hero.
   *
   * <p>Increments attack by 3, and both current and maximum health by 5.
   *
   * @param hero the {@link Hero} to apply the bonus to
   */
  @Override
  public void applyLevelBonus(Hero hero) {

    hero.setAttack(hero.getAttack() + 3);
    hero.setHealth(hero.getHealth() + 5);
    hero.setMaxHealth(hero.getMaxHealth() + 5);
  }
}
