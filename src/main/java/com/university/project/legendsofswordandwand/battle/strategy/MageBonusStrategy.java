package com.university.project.legendsofswordandwand.battle.strategy;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import org.springframework.stereotype.Component;

/**
 * {@link ClassBonusStrategy} implementation for the {@code MAGE} hero class.
 *
 * <p>On each level gained in the Mage class, the hero receives:
 *
 * <ul>
 *   <li>+5 mana (current and maximum)
 *   <li>+1 attack
 * </ul>
 */
@Component
public class MageBonusStrategy implements ClassBonusStrategy {

  /**
   * Returns {@link HeroClass#MAGE}.
   *
   * @return {@code HeroClass.MAGE}
   */
  @Override
  public HeroClass getHeroClass() {

    return HeroClass.MAGE;
  }

  /**
   * Applies the Mage class level bonus to the given hero.
   *
   * <p>Increments both current and maximum mana by 5, and attack by 1.
   *
   * @param hero the {@link Hero} to apply the bonus to
   */
  @Override
  public void applyLevelBonus(Hero hero) {

    hero.setMana(hero.getMana() + 5);
    hero.setMaxMana(hero.getMaxMana() + 5);
    hero.setAttack(hero.getAttack() + 1);
  }
}
