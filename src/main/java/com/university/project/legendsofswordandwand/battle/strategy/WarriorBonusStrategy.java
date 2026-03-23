package com.university.project.legendsofswordandwand.battle.strategy;

import com.university.project.legendsofswordandwand.model.Hero;
import com.university.project.legendsofswordandwand.model.enums.HeroClass;
import org.springframework.stereotype.Component;

/**
 * {@link ClassBonusStrategy} implementation for the {@code WARRIOR} hero class.
 *
 * <p>On each level gained in the Warrior class, the hero receives:
 *
 * <ul>
 *   <li>+2 attack
 *   <li>+3 defense
 * </ul>
 */
@Component
public class WarriorBonusStrategy implements ClassBonusStrategy {

  /**
   * Returns {@link HeroClass#WARRIOR}.
   *
   * @return {@code HeroClass.WARRIOR}
   */
  @Override
  public HeroClass getHeroClass() {

    return HeroClass.WARRIOR;
  }

  /**
   * Applies the Warrior class level bonus to the given hero.
   *
   * <p>Increments attack by 2 and defense by 3.
   *
   * @param hero the {@link Hero} to apply the bonus to
   */
  @Override
  public void applyLevelBonus(Hero hero) {

    hero.setAttack(hero.getAttack() + 2);
    hero.setDefense(hero.getDefense() + 3);
  }
}
