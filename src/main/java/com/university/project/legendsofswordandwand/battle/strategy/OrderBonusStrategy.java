package com.university.project.legendsofswordandwand.battle.strategy;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import org.springframework.stereotype.Component;

/**
 * {@link ClassBonusStrategy} implementation for the {@code ORDER} hero class.
 *
 * <p>On each level gained in the Order class, the hero receives:
 *
 * <ul>
 *   <li>+5 mana (current and maximum)
 *   <li>+2 defense
 * </ul>
 */
@Component
public class OrderBonusStrategy implements ClassBonusStrategy {

  /**
   * Returns {@link HeroClass#ORDER}.
   *
   * @return {@code HeroClass.ORDER}
   */
  @Override
  public HeroClass getHeroClass() {

    return HeroClass.ORDER;
  }

  /**
   * Applies the Order class level bonus to the given hero.
   *
   * <p>Increments both current and maximum mana by 5, and defense by 2.
   *
   * @param hero the {@link Hero} to apply the bonus to
   */
  @Override
  public void applyLevelBonus(Hero hero) {

    hero.setMana(hero.getMana() + 5);
    hero.setMaxMana(hero.getMaxMana() + 5);
    hero.setDefense(hero.getDefense() + 2);
  }
}
